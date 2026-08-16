package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InterviewServiceImpl implements InterviewService {
    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);
    private static final int MAX_FOLLOWUPS_PER_BASE_QUESTION = 2;
    private static final int RECENT_CONTEXT_LIMIT = 3;
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

    @Override @Transactional
    public InterviewSessionDto createTargetedPractice(CreateTargetedPracticeRequest request) {
        User user=authenticatedUsers.getAuthenticatedUser();
        if(request==null || request.focusArea()==null || request.focusArea().isBlank() || request.focusArea().trim().length()>200 || request.difficulty()==null || request.questionCount()==null || (request.questionCount()!=3 && request.questionCount()!=5))
            throw new InvalidResumeException("Targeted practice settings are invalid");
        InterviewSession source=null;
        if(request.sourceInterviewId()!=null) source=sessions.findByIdAndUserId(request.sourceInterviewId(),user.getId()).orElseThrow(()->new ResourceNotFoundException("Source interview not found"));
        String focus=request.focusArea().trim(); String skill=request.skill()==null||request.skill().isBlank()?null:request.skill().trim();
        log.info("Generating targeted-practice questions: focusArea={}, skill={}, difficulty={}, questionCount={}",
                focus, skill, request.difficulty(), request.questionCount());
        AiTargetedPracticeResponse generated=aiClient.generateTargetedPracticeQuestions(new AiTargetedPracticeRequest(focus,skill,request.difficulty().name(),request.questionCount(),request.weaknessContext(),List.of()));
        if(generated==null || generated.questions()==null || generated.questions().size()!=request.questionCount()) throw new AiAnalysisException("AI service returned invalid targeted practice questions");
        Set<String> unique=new HashSet<>();
        for(AiTargetedPracticeQuestion item:generated.questions()) if(item==null||item.question()==null||item.question().isBlank()||item.category()==null||!Set.of("TECHNICAL","HR","PROJECT").contains(item.category().trim().toUpperCase(Locale.ROOT))||item.difficulty()==null||!item.difficulty().equals(request.difficulty().name())||!unique.add(item.question().trim().toLowerCase(Locale.ROOT))) throw new AiAnalysisException("AI service returned invalid targeted practice questions");
        InterviewSession session=new InterviewSession(user,null,InterviewType.TECHNICAL,request.difficulty(),request.questionCount(),lists.serialize(skill==null?List.of():List.of(skill)));
        session.setSessionMode(InterviewSessionMode.TARGETED_PRACTICE); session.setTargetFocusArea(focus); session.setTargetSkill(skill); session.setSourceInterviewId(source==null?null:source.getId()); session.setStatus(InterviewSessionStatus.IN_PROGRESS); session.setStartedAt(LocalDateTime.now());
        InterviewSession saved;
        try {
            saved = sessions.saveAndFlush(session);
        } catch (DataAccessException exception) {
            log.error("Targeted-practice session persistence failed", exception);
            throw exception;
        }
        List<InterviewQuestionRecord> records=new ArrayList<>();
        for(int i=0;i<generated.questions().size();i++){AiTargetedPracticeQuestion item=generated.questions().get(i); InterviewQuestionRecord record=new InterviewQuestionRecord(saved,"targeted-"+UUID.randomUUID(),item.question().trim(),item.category().trim().toUpperCase(Locale.ROOT),item.skill()==null?skill:item.skill(),request.difficulty(),i+1); record.setFocusArea(item.focusConcept()==null?focus:item.focusConcept()); records.add(record);}
        try {
            questions.saveAll(records);
        } catch (DataAccessException exception) {
            log.error("Targeted-practice question persistence failed: sessionId={}, questionCount={}",
                    saved.getId(), records.size(), exception);
            throw exception;
        }
        return toDto(saved,records);
    }

    @Override @Transactional(readOnly=true)
    public TargetedPracticeSummaryDto getTargetedPracticeSummary(Long sessionId){
        InterviewSession session=ownedSession(sessionId); if(session.getSessionMode()!=InterviewSessionMode.TARGETED_PRACTICE) throw new InvalidResumeException("Targeted summary is only available for targeted practice sessions");
        List<InterviewQuestionRecord> records=questions.findBySessionIdOrderByQuestionOrder(sessionId); List<InterviewAnswerEvaluation> current=evaluations.findByQuestionSessionIdAndStatus(sessionId,EvaluationStatus.COMPLETED);
        List<InterviewAnswerEvaluation> baseline=List.of();
        if(session.getSourceInterviewId()!=null){InterviewSession source=sessions.findByIdAndUserId(session.getSourceInterviewId(),session.getUser().getId()).orElse(null); if(source!=null){String key=session.getTargetSkill()!=null?session.getTargetSkill():session.getTargetFocusArea(); baseline=evaluations.findByQuestionSessionIdAndStatus(source.getId(),EvaluationStatus.COMPLETED).stream().filter(e->matchesFocus(e.getQuestion(),key)).toList();}}
        Double avg=average(current.stream().map(InterviewAnswerEvaluation::getOverallScore).toList()); Double base=average(baseline.stream().map(InterviewAnswerEvaluation::getOverallScore).toList());
        return new TargetedPracticeSummaryDto(sessionId,session.getTargetFocusArea(),session.getTargetSkill(),session.getTotalQuestions(),(int)records.stream().filter(q->q.getAnswerText()!=null&&!q.getAnswerText().isBlank()).count(),current.size(),avg,base,avg!=null&&base!=null?Math.round((avg-base)*100.0)/100.0:null,rubrics(current),base==null?null:rubrics(baseline),session.getSourceInterviewId());
    }
    private boolean matchesFocus(InterviewQuestionRecord q,String key){if(key==null)return false; return (q.getSkill()!=null&&q.getSkill().equalsIgnoreCase(key))||(q.getFocusArea()!=null&&q.getFocusArea().equalsIgnoreCase(key))||(q.getCategory()!=null&&q.getCategory().equalsIgnoreCase(key));}
    private Double average(List<Integer> values){List<Integer> valid=values.stream().filter(Objects::nonNull).toList(); return valid.isEmpty()?null:Math.round(valid.stream().mapToInt(Integer::intValue).average().orElse(0)*100.0)/100.0;}
    private RubricAveragesDto rubrics(List<InterviewAnswerEvaluation> values){return new RubricAveragesDto(average(values.stream().map(InterviewAnswerEvaluation::getRelevanceScore).toList()),average(values.stream().map(InterviewAnswerEvaluation::getCorrectnessScore).toList()),average(values.stream().map(InterviewAnswerEvaluation::getCompletenessScore).toList()),average(values.stream().map(InterviewAnswerEvaluation::getCommunicationScore).toList()));}

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

    @Override @Transactional
    public FollowUpGenerationDto generateFollowUpQuestion(Long sessionId, Long questionId) {
        InterviewSession session = ownedSession(sessionId);
        if(session.getSessionMode()==InterviewSessionMode.TARGETED_PRACTICE) throw new InvalidResumeException("Adaptive follow-ups are disabled for targeted practice");
        if (session.getStatus() == InterviewSessionStatus.COMPLETED || session.getStatus() == InterviewSessionStatus.ABANDONED)
            throw new InvalidResumeException("This interview session no longer accepts follow-ups");
        InterviewQuestionRecord current = questions.findByIdAndSessionId(questionId, session.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found"));
        if (current.getAnswerText() == null || current.getAnswerText().isBlank())
            throw new InvalidResumeException("Save an answer before requesting a follow-up");

        List<InterviewQuestionRecord> ordered = new ArrayList<>(questions.findBySessionIdOrderByQuestionOrder(session.getId()));
        Long baseId = Boolean.TRUE.equals(current.getAdaptive()) ? current.getParentQuestionId() : current.getId();
        List<InterviewQuestionRecord> existing = ordered.stream()
                .filter(value -> Boolean.TRUE.equals(value.getAdaptive()) && Objects.equals(value.getParentQuestionId(), baseId)).toList();
        if (existing.size() >= MAX_FOLLOWUPS_PER_BASE_QUESTION)
            return new FollowUpGenerationDto(false, null, "Maximum follow-up limit reached for this base question.");

        List<AiInterviewContextItem> context = ordered.stream()
                .filter(value -> value.getQuestionOrder() < current.getQuestionOrder())
                .filter(value -> value.getAnswerText() != null && !value.getAnswerText().isBlank())
                .skip(Math.max(0, ordered.stream().filter(value -> value.getQuestionOrder() < current.getQuestionOrder())
                        .filter(value -> value.getAnswerText() != null && !value.getAnswerText().isBlank()).count() - RECENT_CONTEXT_LIMIT))
                .map(value -> new AiInterviewContextItem(value.getQuestionText(), value.getAnswerText())).toList();
        AiFollowUpResponse generated = aiClient.generateFollowUp(new AiFollowUpRequest(current.getQuestionText(),
                current.getAnswerText(), current.getCategory(), current.getSkill(), current.getDifficulty().name(),
                existing.stream().map(InterviewQuestionRecord::getQuestionText).toList(), context));
        if (!Boolean.TRUE.equals(generated.shouldAskFollowup()))
            return new FollowUpGenerationDto(false, null, generated.reason());
        if (generated.question() == null || generated.question().isBlank())
            throw new AiAnalysisException("AI service returned an invalid follow-up question");

        int insertionOrder = Math.max(current.getQuestionOrder(), existing.stream()
                .mapToInt(InterviewQuestionRecord::getQuestionOrder).max().orElse(current.getQuestionOrder())) + 1;
        ordered.forEach(value -> value.setQuestionOrder(-value.getQuestionOrder()));
        questions.saveAllAndFlush(ordered);
        ordered.forEach(value -> value.setQuestionOrder(Math.abs(value.getQuestionOrder()) >= insertionOrder
                ? Math.abs(value.getQuestionOrder()) + 1 : Math.abs(value.getQuestionOrder())));
        questions.saveAllAndFlush(ordered);
        InterviewQuestionRecord followUp = new InterviewQuestionRecord(session, "adaptive-" + UUID.randomUUID(),
                generated.question().trim(), current.getCategory(), current.getSkill(), current.getDifficulty(), insertionOrder);
        followUp.setAdaptive(true); followUp.setParentQuestionId(baseId);
        followUp.setFollowUpDepth(existing.size() + 1);
        followUp.setGenerationReason(generated.reason()); followUp.setFocusArea(generated.focusArea());
        InterviewQuestionRecord saved = questions.save(followUp);
        return new FollowUpGenerationDto(true, toQuestionDto(saved), generated.reason());
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
        List<InterviewQuestionDto> questionDtos = records.stream().map(this::toQuestionDto).toList();
        int answered = (int) records.stream().filter(value -> value.getAnswerText() != null && !value.getAnswerText().isBlank()).count();
        Resume resume = session.getResume();
        return new InterviewSessionDto(session.getId(), resume == null ? null : resume.getId(),
                resume == null ? null : resume.getOriginalFileName(), session.getInterviewType(), session.getDifficulty(),
                session.getTotalQuestions(), lists.deserialize(session.getDetectedSkills()), session.getStatus(), answered,
                session.getStartedAt(), session.getCompletedAt(), session.getCreatedAt(), session.getUpdatedAt(), questionDtos,
                session.getSessionMode(), session.getTargetSkill(), session.getTargetFocusArea(), session.getSourceInterviewId());
    }

    private InterviewQuestionDto toQuestionDto(InterviewQuestionRecord value) {
        return new InterviewQuestionDto(value.getId(), value.getExternalQuestionId(), value.getQuestionText(),
                value.getCategory(), value.getSkill(), value.getDifficulty(), value.getQuestionOrder(),
                value.getAnswerText(), value.getAnsweredAt(), Boolean.TRUE.equals(value.getAdaptive()),
                value.getParentQuestionId(), value.getFollowUpDepth(), value.getFocusArea());
    }
}
