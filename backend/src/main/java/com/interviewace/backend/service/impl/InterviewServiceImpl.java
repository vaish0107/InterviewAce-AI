package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InterviewServiceImpl implements InterviewService {
    private final InterviewSessionRepository sessions;
    private final InterviewQuestionRecordRepository questions;
    private final ResumeRepository resumes;
    private final ResumeAnalysisRepository analyses;
    private final InterviewAnswerEvaluationRepository evaluations;
    private final AuthenticatedUserService authenticatedUsers;
    private final AiResumeClient aiClient;
    private final JsonListMapper lists;
    public InterviewServiceImpl(InterviewSessionRepository sessions, InterviewQuestionRecordRepository questions,
            ResumeRepository resumes, ResumeAnalysisRepository analyses, InterviewAnswerEvaluationRepository evaluations, AuthenticatedUserService authenticatedUsers,
            AiResumeClient aiClient, JsonListMapper lists) {
        this.sessions = sessions; this.questions = questions; this.resumes = resumes; this.analyses = analyses; this.evaluations = evaluations;
        this.authenticatedUsers = authenticatedUsers; this.aiClient = aiClient; this.lists = lists;
    }

    @Override @Transactional
    public InterviewSessionDto createInterview(CreateInterviewRequest request) {
        User user = authenticatedUsers.getAuthenticatedUser();
        if (request == null || request.interviewType() == null || request.difficulty() == null
                || request.questionCount() == null || request.questionCount() < 3 || request.questionCount() > 20)
            throw new InvalidResumeException("Interview settings are invalid");
        Resume resume = resolveResume(request.resumeId(), user.getId());
        List<String> skills = resolveSkills(resume, request.interviewType());
        AiQuestionGenerationResponse generated = aiClient.generateInterviewQuestions(new AiQuestionGenerationRequest(
                skills, request.interviewType().name(), request.difficulty().name(), request.questionCount()));
        validateGenerated(generated, request);
        InterviewSession session = new InterviewSession(user, resume, request.interviewType(), request.difficulty(),
                generated.totalQuestions(), lists.serialize(skills));
        session.setStatus(InterviewSessionStatus.IN_PROGRESS); session.setStartedAt(LocalDateTime.now());
        InterviewSession saved = sessions.saveAndFlush(session);
        List<InterviewQuestionRecord> records = new ArrayList<>();
        for (int index = 0; index < generated.questions().size(); index++) {
            AiInterviewQuestionDto item = generated.questions().get(index);
            records.add(new InterviewQuestionRecord(saved, item.id(), item.question(), item.category(), item.skill(),
                    InterviewDifficulty.valueOf(item.difficulty()), index + 1));
        }
        questions.saveAll(records);
        return toDto(saved, records);
    }

    @Override @Transactional(readOnly = true)
    public InterviewSessionDto getInterview(Long id) {
        InterviewSession session = ownedSession(id);
        return toDto(session, questions.findBySessionIdOrderByQuestionOrder(session.getId()));
    }

    @Override @Transactional(readOnly = true)
    public List<InterviewSessionDto> getMyInterviews() {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        return sessions.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(session -> toDto(session, questions.findBySessionIdOrderByQuestionOrder(session.getId()))).toList();
    }

    @Override @Transactional
    public InterviewSessionDto submitAnswer(Long sessionId, Long questionId, SubmitAnswerRequest request) {
        InterviewSession session = ownedSession(sessionId);
        if (session.getStatus() == InterviewSessionStatus.COMPLETED || session.getStatus() == InterviewSessionStatus.ABANDONED)
            throw new InvalidResumeException("This interview session no longer accepts answers");
        String answer = request == null ? null : request.answer();
        if (answer == null || answer.isBlank()) throw new InvalidResumeException("Answer is required");
        InterviewQuestionRecord question = questions.findByIdAndSessionId(questionId, session.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found"));
        evaluations.findByQuestionId(question.getId()).ifPresent(evaluations::delete);
        question.setAnswerText(answer.trim()); question.setAnsweredAt(LocalDateTime.now()); questions.save(question);
        return toDto(session, questions.findBySessionIdOrderByQuestionOrder(session.getId()));
    }

    @Override @Transactional
    public InterviewSessionDto completeInterview(Long sessionId) {
        InterviewSession session = ownedSession(sessionId);
        if (session.getStatus() != InterviewSessionStatus.COMPLETED) {
            session.setStatus(InterviewSessionStatus.COMPLETED); session.setCompletedAt(LocalDateTime.now()); sessions.save(session);
        }
        return toDto(session, questions.findBySessionIdOrderByQuestionOrder(session.getId()));
    }

    private Resume resolveResume(Long resumeId, Long userId) {
        if (resumeId == null) return null;
        return resumes.findByIdAndUserId(resumeId, userId).orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private List<String> resolveSkills(Resume resume, InterviewType type) {
        if (type == InterviewType.HR) {
            if (resume == null) return List.of();
            return analyses.findTopByResumeIdOrderByCreatedAtDesc(resume.getId())
                    .filter(value -> value.getAnalysisStatus() == AnalysisStatus.COMPLETED)
                    .map(value -> lists.deserialize(value.getDetectedSkills())).orElse(List.of());
        }
        if (resume == null) throw new InvalidResumeException("A resume with completed analysis is required for this interview type");
        ResumeAnalysis analysis = analyses.findTopByResumeIdOrderByCreatedAtDesc(resume.getId())
                .filter(value -> value.getAnalysisStatus() == AnalysisStatus.COMPLETED)
                .orElseThrow(() -> new InvalidResumeException("Complete resume analysis before starting this interview"));
        return lists.deserialize(analysis.getDetectedSkills());
    }

    private void validateGenerated(AiQuestionGenerationResponse response, CreateInterviewRequest request) {
        if (response == null || response.questions() == null || response.totalQuestions() == null
                || response.totalQuestions() != request.questionCount() || response.questions().size() != request.questionCount()
                || !request.interviewType().name().equals(response.interviewType())
                || !request.difficulty().name().equals(response.difficulty()))
            throw new AiAnalysisException("AI service returned an invalid question response");
        Set<String> ids = new HashSet<>();
        for (AiInterviewQuestionDto item : response.questions()) {
            if (item == null || item.id() == null || item.question() == null || item.question().isBlank()
                    || item.category() == null || !request.difficulty().name().equals(item.difficulty()) || !ids.add(item.id()))
                throw new AiAnalysisException("AI service returned invalid interview questions");
            try { InterviewDifficulty.valueOf(item.difficulty()); }
            catch (IllegalArgumentException exception) { throw new AiAnalysisException("AI service returned invalid interview questions", exception); }
        }
    }

    private InterviewSession ownedSession(Long id) {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        return sessions.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));
    }

    private InterviewSessionDto toDto(InterviewSession session, List<InterviewQuestionRecord> records) {
        List<InterviewQuestionDto> questionDtos = records.stream().map(value -> new InterviewQuestionDto(value.getId(),
                value.getExternalQuestionId(), value.getQuestionText(), value.getCategory(), value.getSkill(),
                value.getDifficulty(), value.getQuestionOrder(), value.getAnswerText(), value.getAnsweredAt())).toList();
        int answered = (int) records.stream().filter(value -> value.getAnswerText() != null && !value.getAnswerText().isBlank()).count();
        Resume resume = session.getResume();
        return new InterviewSessionDto(session.getId(), resume == null ? null : resume.getId(),
                resume == null ? null : resume.getOriginalFileName(), session.getInterviewType(), session.getDifficulty(),
                session.getTotalQuestions(), lists.deserialize(session.getDetectedSkills()), session.getStatus(), answered,
                session.getStartedAt(), session.getCompletedAt(), session.getCreatedAt(), session.getUpdatedAt(), questionDtos);
    }
}
