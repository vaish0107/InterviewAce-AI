package com.interviewace.backend.service;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.InterviewAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewAnalyticsServiceImplTests {
    private InterviewSessionRepository sessions;
    private InterviewQuestionRecordRepository questions;
    private InterviewAnswerEvaluationRepository evaluations;
    private AuthenticatedUserService users;
    private InterviewAnalyticsServiceImpl service;
    private User user;

    @BeforeEach void setup() {
        sessions = mock(InterviewSessionRepository.class); questions = mock(InterviewQuestionRecordRepository.class);
        evaluations = mock(InterviewAnswerEvaluationRepository.class); users = mock(AuthenticatedUserService.class);
        service = new InterviewAnalyticsServiceImpl(sessions, questions, evaluations, users);
        user = new User(); user.setId(7L); when(users.getAuthenticatedUser()).thenReturn(user);
    }

    @Test void summaryCalculatesOnlyCompletedEvaluationsAndGroupsCategoriesAndSkills() {
        InterviewSession session = session(10L, InterviewSessionStatus.COMPLETED, LocalDateTime.of(2026, 1, 2, 10, 0));
        InterviewQuestionRecord java = question(1L, session, "TECHNICAL", "Java", "answered");
        InterviewQuestionRecord hr = question(2L, session, "HR", null, "answered");
        InterviewQuestionRecord unanswered = question(3L, session, "PROJECT", "Spring", null);
        when(sessions.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(session));
        when(questions.findBySessionIdOrderByQuestionOrder(10L)).thenReturn(List.of(java, hr, unanswered));
        when(evaluations.findByQuestionSessionIdAndStatus(10L, EvaluationStatus.COMPLETED))
                .thenReturn(List.of(evaluation(java, 80, 20, 28, 20, 12), evaluation(hr, 60, 15, 20, 15, 10)));

        InterviewSummaryDto result = service.getInterviewSummary(10L);
        assertEquals(70.0, result.averageScore()); assertEquals(17.5, result.averageRelevance());
        assertEquals(2, result.answeredQuestions()); assertEquals(2, result.evaluatedQuestions());
        assertEquals(1, result.unansweredQuestions()); assertEquals(Map.of("HR", 60.0, "TECHNICAL", 80.0), result.categoryScores());
        assertEquals(Map.of("Java", 80.0), result.skillScores());
        assertEquals("TECHNICAL", result.strongestCategory()); assertEquals("HR", result.weakestCategory());
    }

    @Test void summaryWithNoEvaluationsHasNullAveragesAndNoMeasuredAreas() {
        InterviewSession session = session(10L, InterviewSessionStatus.IN_PROGRESS, null);
        when(sessions.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(session));
        when(questions.findBySessionIdOrderByQuestionOrder(10L)).thenReturn(List.of(question(1L, session, "HR", null, "answer")));
        when(evaluations.findByQuestionSessionIdAndStatus(10L, EvaluationStatus.COMPLETED)).thenReturn(List.of());
        InterviewSummaryDto result = service.getInterviewSummary(10L);
        assertNull(result.averageScore()); assertNull(result.averageCorrectness()); assertTrue(result.categoryScores().isEmpty());
        assertNull(result.strongestCategory()); assertNull(result.weakestCategory());
    }

    @Test void summaryEnforcesOwnership() {
        when(sessions.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getInterviewSummary(99L));
        verifyNoInteractions(questions, evaluations);
    }

    @Test void progressAggregatesUserDataAndOrdersTrendOldestFirst() {
        InterviewSession newer = session(2L, InterviewSessionStatus.COMPLETED, LocalDateTime.of(2026, 2, 1, 10, 0));
        InterviewSession older = session(1L, InterviewSessionStatus.COMPLETED, LocalDateTime.of(2026, 1, 1, 10, 0));
        InterviewSession active = session(3L, InterviewSessionStatus.IN_PROGRESS, null);
        InterviewQuestionRecord q1 = question(1L, older, "HR", null, "answered");
        InterviewQuestionRecord q2 = question(2L, newer, "TECHNICAL", "Java", "answered");
        InterviewQuestionRecord q3 = question(3L, active, "PROJECT", null, null);
        when(sessions.findByUserIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(active, newer, older));
        when(questions.findBySessionUserId(7L)).thenReturn(List.of(q1, q2, q3));
        when(evaluations.findByUserIdAndStatus(7L, EvaluationStatus.COMPLETED))
                .thenReturn(List.of(evaluation(q2, 90, 23, 32, 22, 13), evaluation(q1, 70, 18, 24, 18, 10)));
        InterviewProgressDto result = service.getMyProgress();
        assertEquals(3, result.totalInterviews()); assertEquals(2, result.completedInterviews()); assertEquals(1, result.inProgressInterviews());
        assertEquals(2, result.totalQuestionsAnswered()); assertEquals(2, result.totalEvaluatedAnswers()); assertEquals(80.0, result.overallAverageScore());
        assertEquals(List.of(1L, 2L), result.scoreTrend().stream().map(InterviewTrendPointDto::interviewId).toList());
        assertEquals("TECHNICAL", result.strongestCategory()); assertEquals("HR", result.weakestCategory());
    }

    private InterviewSession session(Long id, InterviewSessionStatus status, LocalDateTime completedAt) {
        InterviewSession value = new InterviewSession(); value.setId(id); value.setUser(user); value.setInterviewType(InterviewType.MIXED);
        value.setDifficulty(InterviewDifficulty.MEDIUM); value.setStatus(status); value.setTotalQuestions(3);
        value.setStartedAt(LocalDateTime.of(2025, 12, 1, 10, 0)); value.setCompletedAt(completedAt); return value;
    }
    private InterviewQuestionRecord question(Long id, InterviewSession session, String category, String skill, String answer) {
        InterviewQuestionRecord value = new InterviewQuestionRecord(); value.setId(id); value.setSession(session);
        value.setCategory(category); value.setSkill(skill); value.setAnswerText(answer); return value;
    }
    private InterviewAnswerEvaluation evaluation(InterviewQuestionRecord question, int total, int relevance, int correctness, int completeness, int communication) {
        InterviewAnswerEvaluation value = new InterviewAnswerEvaluation(question, EvaluationStatus.COMPLETED);
        value.setOverallScore(total); value.setRelevanceScore(relevance); value.setCorrectnessScore(correctness);
        value.setCompletenessScore(completeness); value.setCommunicationScore(communication); return value;
    }
}
