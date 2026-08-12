package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.InterviewEvaluationServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewEvaluationServiceImplTests {
    private InterviewSessionRepository sessions; private InterviewQuestionRecordRepository questions;
    private InterviewAnswerEvaluationRepository evaluations; private AuthenticatedUserService authenticatedUsers;
    private AiResumeClient aiClient; private InterviewEvaluationService service;
    private User user; private InterviewSession session; private InterviewQuestionRecord question;

    @BeforeEach void setUp() {
        sessions = mock(InterviewSessionRepository.class); questions = mock(InterviewQuestionRecordRepository.class);
        evaluations = mock(InterviewAnswerEvaluationRepository.class); authenticatedUsers = mock(AuthenticatedUserService.class);
        aiClient = mock(AiResumeClient.class); user = new User("Jane", "jane@example.com", "hash"); user.setId(1L);
        session = new InterviewSession(user, null, InterviewType.HR, InterviewDifficulty.MEDIUM, 1, "[]"); session.setId(20L);
        question = new InterviewQuestionRecord(session, "q1", "Tell me about yourself.", "HR", null, InterviewDifficulty.MEDIUM, 1);
        question.setId(100L); question.setAnswerText("I build reliable backend systems and enjoy collaborating with teams.");
        when(authenticatedUsers.getAuthenticatedUser()).thenReturn(user);
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(question));
        when(evaluations.saveAndFlush(any())).thenAnswer(invocation -> {
            InterviewAnswerEvaluation value = invocation.getArgument(0); if (value.getId() == null) value.setId(300L);
            if (value.getCreatedAt() == null) value.setCreatedAt(LocalDateTime.now()); return value;
        });
        service = new InterviewEvaluationServiceImpl(sessions, questions, evaluations, authenticatedUsers, aiClient,
                new JsonListMapper(new ObjectMapper()));
    }

    @Test void persistsSuccessfulEvaluation() {
        when(evaluations.findByQuestionId(100L)).thenReturn(Optional.empty()); when(aiClient.evaluateInterviewAnswer(any())).thenReturn(valid());
        InterviewAnswerEvaluationDto result = service.evaluateAnswer(20L, 100L);
        assertEquals(82, result.overallScore()); assertEquals(EvaluationStatus.COMPLETED, result.status());
        assertEquals(List.of("Clear response"), result.strengths()); assertNotNull(result.evaluatedAt());
        verify(evaluations, times(2)).saveAndFlush(any());
    }

    @Test void enforcesSessionOwnership() {
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.evaluateAnswer(20L, 100L)); verifyNoInteractions(aiClient, evaluations);
    }

    @Test void reportsQuestionNotFoundOrSessionMismatch() {
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.evaluateAnswer(20L, 100L)); verifyNoInteractions(aiClient, evaluations);
    }

    @Test void requiresSavedAnswer() {
        question.setAnswerText("  ");
        assertThrows(InvalidResumeException.class, () -> service.evaluateAnswer(20L, 100L)); verifyNoInteractions(aiClient, evaluations);
    }

    @Test void rejectsInvalidReturnedScoresAndPersistsFailure() {
        when(evaluations.findByQuestionId(100L)).thenReturn(Optional.empty());
        AiAnswerEvaluationResponse invalid = new AiAnswerEvaluationResponse(99, 22, 28, 20, 12, List.of(), List.of(), List.of(), "Improved", "Summary", "Note");
        when(aiClient.evaluateInterviewAnswer(any())).thenReturn(invalid);
        assertThrows(AiAnalysisException.class, () -> service.evaluateAnswer(20L, 100L));
        ArgumentCaptor<InterviewAnswerEvaluation> captor = ArgumentCaptor.forClass(InterviewAnswerEvaluation.class);
        verify(evaluations, times(2)).saveAndFlush(captor.capture()); assertEquals(EvaluationStatus.FAILED, captor.getValue().getStatus());
    }

    @Test void aiFailurePersistsSafeFailedStatus() {
        when(evaluations.findByQuestionId(100L)).thenReturn(Optional.empty());
        when(aiClient.evaluateInterviewAnswer(any())).thenThrow(new AiServiceUnavailableException("internal detail"));
        assertThrows(AiServiceUnavailableException.class, () -> service.evaluateAnswer(20L, 100L));
        ArgumentCaptor<InterviewAnswerEvaluation> captor = ArgumentCaptor.forClass(InterviewAnswerEvaluation.class);
        verify(evaluations, times(2)).saveAndFlush(captor.capture());
        assertEquals(EvaluationStatus.FAILED, captor.getValue().getStatus());
        assertEquals("AI answer evaluation service is unavailable", captor.getValue().getFailureMessage());
        assertEquals("I build reliable backend systems and enjoy collaborating with teams.", question.getAnswerText());
    }

    @Test void retryReusesFailedEvaluationAndCompletesIt() {
        InterviewAnswerEvaluation failed = new InterviewAnswerEvaluation(question, EvaluationStatus.FAILED); failed.setId(300L); failed.setFailureMessage("Previous failure");
        when(evaluations.findByQuestionId(100L)).thenReturn(Optional.of(failed)); when(aiClient.evaluateInterviewAnswer(any())).thenReturn(valid());
        InterviewAnswerEvaluationDto result = service.evaluateAnswer(20L, 100L);
        assertEquals(300L, result.id()); assertEquals(EvaluationStatus.COMPLETED, result.status()); assertNull(result.failureMessage());
    }

    @Test void getsPersistedEvaluationAfterOwnershipCheck() {
        InterviewAnswerEvaluation value = completed(); when(evaluations.findByQuestionId(100L)).thenReturn(Optional.of(value));
        InterviewAnswerEvaluationDto result = service.getEvaluation(20L, 100L);
        assertEquals(100L, result.questionId()); assertEquals(List.of("Clear response"), result.strengths());
    }

    private InterviewAnswerEvaluation completed() {
        InterviewAnswerEvaluation value = new InterviewAnswerEvaluation(question, EvaluationStatus.COMPLETED); value.setId(300L);
        AiAnswerEvaluationResponse response = valid(); value.setOverallScore(response.overallScore()); value.setRelevanceScore(response.relevanceScore());
        value.setCorrectnessScore(response.correctnessScore()); value.setCompletenessScore(response.completenessScore());
        value.setCommunicationScore(response.communicationScore()); value.setStrengths("[\"Clear response\"]"); value.setWeaknesses("[]");
        value.setMissingKeyPoints("[]"); value.setImprovedAnswer(response.improvedAnswer()); value.setSummary(response.summary());
        value.setEvaluationNote(response.evaluationNote()); value.setEvaluatedAt(LocalDateTime.now()); return value;
    }
    private AiAnswerEvaluationResponse valid() {
        return new AiAnswerEvaluationResponse(82, 22, 28, 20, 12, List.of("Clear response"),
                List.of("Add one example"), List.of("Specific outcome"), "Improved answer", "Strong response", "AI guidance only");
    }
}
