package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.AuthenticatedUserService;
import com.interviewace.backend.service.InterviewAnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
public class InterviewAnalyticsServiceImpl implements InterviewAnalyticsService {
    private final InterviewSessionRepository sessions;
    private final InterviewQuestionRecordRepository questions;
    private final InterviewAnswerEvaluationRepository evaluations;
    private final AuthenticatedUserService authenticatedUsers;

    public InterviewAnalyticsServiceImpl(InterviewSessionRepository sessions,
            InterviewQuestionRecordRepository questions, InterviewAnswerEvaluationRepository evaluations,
            AuthenticatedUserService authenticatedUsers) {
        this.sessions = sessions;
        this.questions = questions;
        this.evaluations = evaluations;
        this.authenticatedUsers = authenticatedUsers;
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewSummaryDto getInterviewSummary(Long interviewId) {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        InterviewSession session = sessions.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));
        List<InterviewQuestionRecord> records = questions.findBySessionIdOrderByQuestionOrder(interviewId);
        List<InterviewAnswerEvaluation> completed = evaluations
                .findByQuestionSessionIdAndStatus(interviewId, EvaluationStatus.COMPLETED);
        int answered = (int) records.stream().filter(this::isAnswered).count();
        Map<String, Double> categoryScores = groupedAverages(completed, false);
        Map<String, Double> skillScores = groupedAverages(completed, true);
        return new InterviewSummaryDto(session.getId(), session.getInterviewType().name(), session.getDifficulty().name(),
                session.getStatus().name(), records.size(), answered, completed.size(),
                Math.max(0, records.size() - answered), average(completed, InterviewAnswerEvaluation::getOverallScore),
                average(completed, InterviewAnswerEvaluation::getRelevanceScore),
                average(completed, InterviewAnswerEvaluation::getCorrectnessScore),
                average(completed, InterviewAnswerEvaluation::getCompletenessScore),
                average(completed, InterviewAnswerEvaluation::getCommunicationScore), categoryScores, skillScores,
                strongest(categoryScores), weakest(categoryScores), session.getStartedAt(), session.getCompletedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewProgressDto getMyProgress() {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        List<InterviewSession> userSessions = sessions.findByUserIdOrderByCreatedAtDesc(userId);
        List<InterviewQuestionRecord> userQuestions = questions.findBySessionUserId(userId);
        List<InterviewAnswerEvaluation> completed = evaluations.findByUserIdAndStatus(userId, EvaluationStatus.COMPLETED);
        Map<String, Double> categories = groupedAverages(completed, false);
        Map<String, Double> skills = groupedAverages(completed, true);
        Map<Long, List<InterviewAnswerEvaluation>> bySession = completed.stream()
                .collect(Collectors.groupingBy(value -> value.getQuestion().getSession().getId()));
        List<InterviewTrendPointDto> trend = userSessions.stream()
                .filter(value -> value.getStatus() == InterviewSessionStatus.COMPLETED && value.getCompletedAt() != null)
                .filter(value -> bySession.containsKey(value.getId()))
                .sorted(Comparator.comparing(InterviewSession::getCompletedAt).thenComparing(InterviewSession::getId))
                .map(value -> new InterviewTrendPointDto(value.getId(), value.getCompletedAt(),
                        average(bySession.get(value.getId()), InterviewAnswerEvaluation::getOverallScore)))
                .toList();
        int completedInterviews = (int) userSessions.stream()
                .filter(value -> value.getStatus() == InterviewSessionStatus.COMPLETED).count();
        int inProgress = (int) userSessions.stream()
                .filter(value -> value.getStatus() == InterviewSessionStatus.IN_PROGRESS).count();
        int answered = (int) userQuestions.stream().filter(this::isAnswered).count();
        return new InterviewProgressDto(userSessions.size(), completedInterviews, inProgress, answered, completed.size(),
                average(completed, InterviewAnswerEvaluation::getOverallScore),
                average(completed, InterviewAnswerEvaluation::getRelevanceScore),
                average(completed, InterviewAnswerEvaluation::getCorrectnessScore),
                average(completed, InterviewAnswerEvaluation::getCompletenessScore),
                average(completed, InterviewAnswerEvaluation::getCommunicationScore), categories, skills, trend,
                strongest(categories), weakest(categories));
    }

    private boolean isAnswered(InterviewQuestionRecord value) {
        return value.getAnswerText() != null && !value.getAnswerText().isBlank();
    }

    private Double average(List<InterviewAnswerEvaluation> values, ToIntFunction<InterviewAnswerEvaluation> getter) {
        if (values.isEmpty()) return null;
        return round(values.stream().mapToInt(getter).average().orElseThrow());
    }

    private Map<String, Double> groupedAverages(List<InterviewAnswerEvaluation> values, boolean bySkill) {
        return values.stream().filter(value -> {
                    String key = bySkill ? value.getQuestion().getSkill() : value.getQuestion().getCategory();
                    return key != null && !key.isBlank();
                }).collect(Collectors.groupingBy(value -> (bySkill ? value.getQuestion().getSkill()
                                : value.getQuestion().getCategory()).trim(), TreeMap::new,
                        Collectors.averagingInt(InterviewAnswerEvaluation::getOverallScore)))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> round(value.getValue()),
                        (left, right) -> left, LinkedHashMap::new));
    }

    private String strongest(Map<String, Double> values) {
        if (values.size() < 2) return null;
        return values.entrySet().stream().max(Map.Entry.<String, Double>comparingByValue()
                .thenComparing(Map.Entry::getKey, Comparator.reverseOrder())).orElseThrow().getKey();
    }

    private String weakest(Map<String, Double> values) {
        if (values.size() < 2) return null;
        return values.entrySet().stream().min(Map.Entry.<String, Double>comparingByValue()
                .thenComparing(Map.Entry::getKey)).orElseThrow().getKey();
    }

    private Double round(double value) { return Math.round(value * 10.0) / 10.0; }
}
