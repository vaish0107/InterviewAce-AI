package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.JobMatchServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobMatchServiceImplTests {
    private static final String DESCRIPTION = "Java Spring Boot developer with PostgreSQL experience";
    @TempDir Path temporaryDirectory;
    private ResumeRepository resumes; private JobMatchAnalysisRepository matches;
    private AuthenticatedUserService authenticatedUsers; private AiResumeClient aiClient;
    private JobMatchService service; private User user;

    @BeforeEach void setUp() {
        resumes = mock(ResumeRepository.class); matches = mock(JobMatchAnalysisRepository.class);
        authenticatedUsers = mock(AuthenticatedUserService.class); aiClient = mock(AiResumeClient.class);
        user = new User("Jane", "jane@example.com", "hash"); user.setId(1L);
        when(authenticatedUsers.getAuthenticatedUser()).thenReturn(user);
        when(matches.saveAndFlush(any())).thenAnswer(invocation -> {
            JobMatchAnalysis value = invocation.getArgument(0); value.setId(70L); value.setCreatedAt(LocalDateTime.now()); return value;
        });
        service = new JobMatchServiceImpl(resumes, matches, authenticatedUsers, aiClient,
                new JsonListMapper(new ObjectMapper()), new JobMatchJsonMapper(new ObjectMapper()));
    }

    @ParameterizedTest @ValueSource(ints = {0, 50, 100})
    void persistsZeroPartialAndFullMatches(int percentage) throws Exception {
        Resume resume = existingResume(); own(resume); when(aiClient.matchJob(any(), eq(DESCRIPTION))).thenReturn(response(percentage));
        JobMatchAnalysisDto result = service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION));
        assertEquals(percentage, result.matchPercentage()); assertEquals(List.of("Java"), result.matchedSkills());
        verify(matches).saveAndFlush(argThat(value -> value.getCategoryMatches().contains("backend")));
    }

    @Test void protectsOwnership() {
        when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION)));
        verifyNoInteractions(aiClient, matches);
    }
    @Test void reportsResumeNotFoundForLatest() {
        assertThrows(ResourceNotFoundException.class, () -> service.getLatestJobMatch(99L));
    }
    @Test void rejectsBlankDescriptionWithoutCallingAi() throws Exception {
        own(existingResume());
        assertThrows(InvalidResumeException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest("  ")));
        verifyNoInteractions(aiClient);
    }
    @Test void rejectsMissingPhysicalFile() {
        own(resume(temporaryDirectory.resolve("missing.pdf")));
        assertThrows(AiAnalysisException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION)));
        verifyNoInteractions(aiClient);
    }
    @Test void propagatesAiServiceUnavailableWithoutPersisting() throws Exception {
        own(existingResume()); when(aiClient.matchJob(any(), anyString())).thenThrow(new AiServiceUnavailableException("AI analysis service is unavailable"));
        assertThrows(AiServiceUnavailableException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION)));
        verifyNoInteractions(matches);
    }
    @Test void rejectsInvalidMatchPercentage() throws Exception {
        own(existingResume()); when(aiClient.matchJob(any(), anyString())).thenReturn(response(101));
        assertThrows(AiAnalysisException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION)));
        verifyNoInteractions(matches);
    }
    @Test void rejectsMalformedCategoryData() throws Exception {
        own(existingResume()); AiJobMatchResponse base = response(50);
        AiJobMatchResponse malformed = new AiJobMatchResponse(50, 2, 2, base.matchedSkills(), base.missingSkills(),
                base.additionalResumeSkills(), Map.of("backend", new AiCategoryMatchDto(null, List.of(), List.of(), 50)),
                base.strengths(), base.recommendations(), base.matchingNote());
        when(aiClient.matchJob(any(), anyString())).thenReturn(malformed);
        assertThrows(AiAnalysisException.class, () -> service.analyzeJobMatch(10L, new JobMatchRequest(DESCRIPTION)));
    }
    @Test void returnsLatestPersistedMatch() throws Exception {
        Resume resume = existingResume(); own(resume); JobMatchAnalysis entity = entity(resume, 88, LocalDateTime.now());
        when(matches.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.of(entity));
        assertEquals(88, service.getLatestJobMatch(10L).matchPercentage());
    }
    @Test void returnsRepositoryHistoryOrder() throws Exception {
        Resume resume = existingResume(); own(resume);
        JobMatchAnalysis newest = entity(resume, 90, LocalDateTime.now());
        JobMatchAnalysis older = entity(resume, 40, LocalDateTime.now().minusDays(1));
        when(matches.findByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(newest, older));
        List<JobMatchAnalysisDto> result = service.getJobMatchHistory(10L);
        assertEquals(List.of(90, 40), result.stream().map(JobMatchAnalysisDto::matchPercentage).toList());
        verify(matches).findByResumeIdOrderByCreatedAtDesc(10L);
    }

    private void own(Resume resume) { when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(resume)); }
    private Resume existingResume() throws Exception { Path path = temporaryDirectory.resolve(UUID.randomUUID() + ".pdf"); Files.writeString(path, "%PDF-1.4"); return resume(path); }
    private Resume resume(Path path) { Resume value = new Resume(user, "resume.pdf", "stored.pdf", path.toString(), "application/pdf", 8L); value.setId(10L); return value; }
    private AiJobMatchResponse response(int percentage) {
        AiCategoryMatchDto category = new AiCategoryMatchDto(List.of("Java", "AWS"), List.of("Java"), List.of("AWS"), percentage);
        return new AiJobMatchResponse(percentage, 2, 2, List.of("Java"), List.of("AWS"), List.of("Docker"),
                Map.of("backend", category), List.of("Java matches"), List.of("Learn AWS"), "Explicit skills only");
    }
    private JobMatchAnalysis entity(Resume resume, int percentage, LocalDateTime createdAt) {
        JobMatchAnalysis value = new JobMatchAnalysis(resume, DESCRIPTION); value.setId((long) percentage);
        value.setMatchPercentage(percentage); value.setResumeSkillCount(2); value.setJobSkillCount(2);
        value.setMatchedSkills("[\"Java\"]"); value.setMissingSkills("[\"AWS\"]"); value.setAdditionalResumeSkills("[]");
        value.setCategoryMatches("{}"); value.setStrengths("[]"); value.setRecommendations("[]");
        value.setMatchingNote("Explicit skills only"); value.setCreatedAt(createdAt); return value;
    }
}
