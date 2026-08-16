package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class InterviewEvaluationServiceImpl implements InterviewEvaluationService {
    private static final java.util.Set<String> VALID_CATEGORIES = java.util.Set.of("TECHNICAL", "HR", "PROJECT");
    private final InterviewSessionRepository sessions;
    private final InterviewQuestionRecordRepository questions;
    private final InterviewAnswerEvaluationRepository evaluations;
    private final AuthenticatedUserService authenticatedUsers;
    private final AiResumeClient aiClient;
    private final JsonListMapper lists;
    public InterviewEvaluationServiceImpl(InterviewSessionRepository sessions,
            InterviewQuestionRecordRepository questions, InterviewAnswerEvaluationRepository evaluations,
            AuthenticatedUserService authenticatedUsers, AiResumeClient aiClient, JsonListMapper lists) {
        this.sessions = sessions; this.questions = questions; this.evaluations = evaluations;
        this.authenticatedUsers = authenticatedUsers; this.aiClient = aiClient; this.lists = lists;
    }

    @Override
    public InterviewAnswerEvaluationDto evaluateAnswer(Long sessionId, Long questionId) {
        InterviewQuestionRecord question = ownedQuestion(sessionId, questionId);
        if (question.getAnswerText() == null || question.getAnswerText().isBlank())
            throw new InvalidResumeException("Save an answer before requesting evaluation");
        if (question.getAnswerText().trim().length() < 10)
            throw new InvalidResumeException("Answer must contain at least 10 characters before evaluation");
        InterviewAnswerEvaluation evaluation = evaluations.findByQuestionId(questionId)
                .orElseGet(() -> new InterviewAnswerEvaluation(question, EvaluationStatus.PROCESSING));
        evaluation.setStatus(EvaluationStatus.PROCESSING); evaluation.setFailureMessage(null); evaluations.saveAndFlush(evaluation);
        try {
            AiAnswerEvaluationResponse response = aiClient.evaluateInterviewAnswer(new AiAnswerEvaluationRequest(
                    question.getQuestionText().trim(), question.getAnswerText().trim(), evaluationCategory(question), normalizeSkill(question.getSkill()),
                    question.getDifficulty().name()));
            validate(response);
            evaluation.setOverallScore(response.overallScore()); evaluation.setRelevanceScore(response.relevanceScore());
            evaluation.setCorrectnessScore(response.correctnessScore()); evaluation.setCompletenessScore(response.completenessScore());
            evaluation.setCommunicationScore(response.communicationScore()); evaluation.setStrengths(lists.serialize(response.strengths()));
            evaluation.setWeaknesses(lists.serialize(response.weaknesses())); evaluation.setMissingKeyPoints(lists.serialize(response.missingKeyPoints()));
            evaluation.setImprovedAnswer(response.improvedAnswer()); evaluation.setSummary(response.summary());
            evaluation.setEvaluationNote(response.evaluationNote()); evaluation.setStatus(EvaluationStatus.COMPLETED);
            evaluation.setEvaluatedAt(LocalDateTime.now()); evaluation.setFailureMessage(null);
            return toDto(evaluations.saveAndFlush(evaluation));
        } catch (RuntimeException exception) {
            evaluation.setStatus(EvaluationStatus.FAILED); evaluation.setFailureMessage(safeMessage(exception));
            evaluations.saveAndFlush(evaluation);
            if (exception instanceof AiServiceUnavailableException || exception instanceof AiAnalysisException) throw exception;
            throw new AiAnalysisException(evaluation.getFailureMessage(), exception);
        }
    }

    @Override
    public InterviewAnswerEvaluationDto getEvaluation(Long sessionId, Long questionId) {
        ownedQuestion(sessionId, questionId);
        return evaluations.findByQuestionId(questionId).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Answer evaluation not found"));
    }

    private InterviewQuestionRecord ownedQuestion(Long sessionId, Long questionId) {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        InterviewSession session = sessions.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));
        return questions.findByIdAndSessionId(questionId, session.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview question not found"));
    }

    private void validate(AiAnswerEvaluationResponse value) {
        if (value == null || !inRange(value.relevanceScore(), 25) || !inRange(value.correctnessScore(), 35)
                || !inRange(value.completenessScore(), 25) || !inRange(value.communicationScore(), 15)
                || !inRange(value.overallScore(), 100))
            throw new AiAnalysisException("AI service returned invalid evaluation scores");
        int total = value.relevanceScore() + value.correctnessScore() + value.completenessScore() + value.communicationScore();
        if (value.overallScore() != total) throw new AiAnalysisException("AI service returned inconsistent evaluation scores");
        if (value.strengths() == null || value.weaknesses() == null || value.missingKeyPoints() == null
                || value.improvedAnswer() == null || value.summary() == null || value.evaluationNote() == null)
            throw new AiAnalysisException("AI service returned an invalid answer evaluation");
    }

    private boolean inRange(Integer value, int maximum) { return value != null && value >= 0 && value <= maximum; }
    private String evaluationCategory(InterviewQuestionRecord question) {
        String category = question.getCategory() == null ? "" : question.getCategory().trim().toUpperCase(java.util.Locale.ROOT);
        if (VALID_CATEGORIES.contains(category)) return category;
        if (question.getSession().getSessionMode() == InterviewSessionMode.TARGETED_PRACTICE)
            return question.getSession().getInterviewType() == InterviewType.HR ? "HR" : "TECHNICAL";
        throw new AiAnalysisException("Interview question has an invalid evaluation category");
    }
    private String normalizeSkill(String skill) { return skill == null || skill.isBlank() ? null : skill.trim(); }
    private String safeMessage(RuntimeException exception) {
        if (exception instanceof AiServiceUnavailableException) return "AI answer evaluation service is unavailable";
        if (exception instanceof AiAnalysisException) return exception.getMessage();
        return "Answer evaluation could not be completed";
    }
    private InterviewAnswerEvaluationDto toDto(InterviewAnswerEvaluation value) {
        return new InterviewAnswerEvaluationDto(value.getId(), value.getQuestion().getId(), value.getOverallScore(),
                value.getRelevanceScore(), value.getCorrectnessScore(), value.getCompletenessScore(),
                value.getCommunicationScore(), lists.deserialize(value.getStrengths()), lists.deserialize(value.getWeaknesses()),
                lists.deserialize(value.getMissingKeyPoints()), value.getImprovedAnswer(), value.getSummary(),
                value.getEvaluationNote(), value.getStatus(), value.getEvaluatedAt(), value.getFailureMessage());
    }
}
