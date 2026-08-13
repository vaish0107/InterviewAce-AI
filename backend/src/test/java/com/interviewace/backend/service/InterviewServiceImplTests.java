package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.InterviewServiceImpl;
import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewServiceImplTests {
    private InterviewSessionRepository sessions; private InterviewQuestionRecordRepository questions;
    private ResumeRepository resumes; private ResumeAnalysisRepository analyses;
    private InterviewAnswerEvaluationRepository evaluations;
    private AuthenticatedUserService authenticatedUsers; private AiResumeClient aiClient;
    private InterviewService service; private User user; private Resume resume;

    @BeforeEach void setUp() {
        sessions = mock(InterviewSessionRepository.class); questions = mock(InterviewQuestionRecordRepository.class);
        resumes = mock(ResumeRepository.class); analyses = mock(ResumeAnalysisRepository.class); evaluations = mock(InterviewAnswerEvaluationRepository.class);
        authenticatedUsers = mock(AuthenticatedUserService.class); aiClient = mock(AiResumeClient.class);
        user = new User("Jane", "jane@example.com", "hash"); user.setId(1L);
        resume = new Resume(user, "resume.pdf", "stored.pdf", "stored.pdf", "application/pdf", 10L); resume.setId(10L);
        when(authenticatedUsers.getAuthenticatedUser()).thenReturn(user);
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> {
            InterviewSession value = invocation.getArgument(0); value.setId(20L); value.setCreatedAt(LocalDateTime.now()); value.setUpdatedAt(LocalDateTime.now()); return value;
        });
        when(questions.saveAll(any())).thenAnswer(invocation -> {
            List<InterviewQuestionRecord> values = invocation.getArgument(0);
            for (int index = 0; index < values.size(); index++) values.get(index).setId(100L + index);
            return values;
        });
        when(questions.save(any(InterviewQuestionRecord.class))).thenAnswer(invocation -> {
            InterviewQuestionRecord value = invocation.getArgument(0); if (value.getId() == null) value.setId(500L); return value;
        });
        service = new InterviewServiceImpl(sessions, questions, resumes, analyses, evaluations, authenticatedUsers, aiClient,
                new JsonListMapper(new ObjectMapper()));
    }

    @Test void createsTechnicalSessionFromOwnedCompletedAnalysis() {
        when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(resume));
        ResumeAnalysis analysis = new ResumeAnalysis(resume, AnalysisStatus.COMPLETED); analysis.setDetectedSkills("[\"Java\",\"Spring Boot\"]");
        when(analyses.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.of(analysis));
        when(aiClient.generateInterviewQuestions(any())).thenReturn(generated(3, "TECHNICAL", "MEDIUM"));
        InterviewSessionDto result = service.createInterview(new CreateInterviewRequest(10L, InterviewType.TECHNICAL, InterviewDifficulty.MEDIUM, 3));
        assertEquals(InterviewSessionStatus.IN_PROGRESS, result.status()); assertEquals(List.of("Java", "Spring Boot"), result.detectedSkills());
        assertEquals(3, result.questions().size()); verify(questions).saveAll(any());
    }

    @Test void createsHrSessionWithoutResumeOrSkills() {
        when(aiClient.generateInterviewQuestions(any())).thenReturn(generated(3, "HR", "EASY"));
        InterviewSessionDto result = service.createInterview(new CreateInterviewRequest(null, InterviewType.HR, InterviewDifficulty.EASY, 3));
        assertNull(result.resumeId()); assertTrue(result.detectedSkills().isEmpty()); verifyNoInteractions(resumes, analyses);
    }

    @Test void technicalSessionRequiresCompletedAnalysis() {
        when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(resume));
        when(analyses.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.empty());
        assertThrows(InvalidResumeException.class, () -> service.createInterview(new CreateInterviewRequest(10L, InterviewType.TECHNICAL, InterviewDifficulty.MEDIUM, 3)));
        verifyNoInteractions(aiClient);
    }

    @Test void rejectsMalformedQuestionResponse() {
        when(aiClient.generateInterviewQuestions(any())).thenReturn(generated(2, "HR", "EASY"));
        assertThrows(AiAnalysisException.class, () -> service.createInterview(new CreateInterviewRequest(null, InterviewType.HR, InterviewDifficulty.EASY, 3)));
        verifyNoInteractions(sessions);
    }

    @Test void ownershipHidesOtherUsersSession() {
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getInterview(20L));
    }

    @Test void savesAnswerWithoutScoreOrEvaluation() {
        InterviewSession session = session(); InterviewQuestionRecord question = record(session); question.setId(100L);
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(question));
        when(questions.findBySessionIdOrderByQuestionOrder(20L)).thenReturn(List.of(question));
        InterviewSessionDto result = service.submitAnswer(20L, 100L, new SubmitAnswerRequest("  My saved answer  "));
        assertEquals("My saved answer", result.questions().getFirst().answerText()); assertEquals(1, result.answeredQuestions());
        assertNotNull(result.questions().getFirst().answeredAt()); verify(questions).save(question);
    }

    @Test void completesOwnedSession() {
        InterviewSession session = session(); when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findBySessionIdOrderByQuestionOrder(20L)).thenReturn(List.of(record(session)));
        InterviewSessionDto result = service.completeInterview(20L);
        assertEquals(InterviewSessionStatus.COMPLETED, result.status()); assertNotNull(result.completedAt()); verify(sessions).save(session);
    }

    @Test void persistsAdaptiveFollowUpDirectlyAfterParent() {
        InterviewSession session = session(); InterviewQuestionRecord parent = record(session); parent.setId(100L); parent.setAnswerText("Spring supplies dependencies from outside the class.");
        InterviewQuestionRecord next = new InterviewQuestionRecord(session, "q2", "Next base", "TECHNICAL", "Java", InterviewDifficulty.MEDIUM, 2); next.setId(101L);
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(parent));
        when(questions.findBySessionIdOrderByQuestionOrder(20L)).thenReturn(List.of(parent, next));
        when(aiClient.generateFollowUp(any())).thenReturn(new AiFollowUpResponse(true, "Why prefer constructor injection?", "Missing implementation choice.", "Spring DI"));
        FollowUpGenerationDto result = service.generateFollowUpQuestion(20L, 100L);
        assertTrue(result.created()); assertTrue(result.question().adaptive()); assertEquals(100L, result.question().parentQuestionId());
        assertEquals(2, result.question().questionOrder()); assertEquals(3, next.getQuestionOrder());
    }

    @Test void followUpRequiresSavedAnswer() {
        InterviewSession session = session(); InterviewQuestionRecord parent = record(session); parent.setId(100L);
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(parent));
        assertThrows(InvalidResumeException.class, () -> service.generateFollowUpQuestion(20L, 100L));
        verify(aiClient, never()).generateFollowUp(any());
    }

    @Test void enforcesTwoFollowUpsWithoutCallingAi() {
        InterviewSession session = session(); InterviewQuestionRecord parent = record(session); parent.setId(100L); parent.setAnswerText("A sufficiently detailed saved answer.");
        InterviewQuestionRecord first = adaptive(session, 201L, 100L, 2); InterviewQuestionRecord second = adaptive(session, 202L, 100L, 3);
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(parent));
        when(questions.findBySessionIdOrderByQuestionOrder(20L)).thenReturn(List.of(parent, first, second));
        FollowUpGenerationDto result = service.generateFollowUpQuestion(20L, 100L);
        assertFalse(result.created()); assertTrue(result.reason().contains("Maximum")); verify(aiClient, never()).generateFollowUp(any());
    }

    @Test void noFollowUpDecisionDoesNotPersistQuestion() {
        InterviewSession session = session(); InterviewQuestionRecord parent = record(session); parent.setId(100L); parent.setAnswerText("A complete and sufficiently detailed answer.");
        when(sessions.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(session));
        when(questions.findByIdAndSessionId(100L, 20L)).thenReturn(Optional.of(parent));
        when(questions.findBySessionIdOrderByQuestionOrder(20L)).thenReturn(List.of(parent));
        when(aiClient.generateFollowUp(any())).thenReturn(new AiFollowUpResponse(false, null, "No useful follow-up needed.", null));
        FollowUpGenerationDto result = service.generateFollowUpQuestion(20L, 100L);
        assertFalse(result.created()); verify(questions, never()).save(argThat(value -> Boolean.TRUE.equals(value.getAdaptive())));
    }

    private InterviewSession session() {
        InterviewSession value = new InterviewSession(user, resume, InterviewType.MIXED, InterviewDifficulty.MEDIUM, 1, "[]");
        value.setId(20L); value.setStatus(InterviewSessionStatus.IN_PROGRESS); value.setCreatedAt(LocalDateTime.now()); value.setUpdatedAt(LocalDateTime.now()); return value;
    }
    private InterviewQuestionRecord record(InterviewSession session) {
        return new InterviewQuestionRecord(session, "q1", "Tell me about yourself.", "HR", null, InterviewDifficulty.MEDIUM, 1);
    }
    private InterviewQuestionRecord adaptive(InterviewSession session, Long id, Long parentId, int order) {
        InterviewQuestionRecord value = new InterviewQuestionRecord(session, "adaptive-" + id, "Follow up?", "HR", null, InterviewDifficulty.MEDIUM, order);
        value.setId(id); value.setAdaptive(true); value.setParentQuestionId(parentId); value.setFollowUpDepth(order - 1); return value;
    }
    private AiQuestionGenerationResponse generated(int count, String type, String difficulty) {
        List<AiInterviewQuestionDto> values = new ArrayList<>();
        for (int index = 1; index <= count; index++) values.add(new AiInterviewQuestionDto("q" + index, "Question " + index, "TECHNICAL", "Java", difficulty));
        return new AiQuestionGenerationResponse(type, difficulty, count, values);
    }
}
