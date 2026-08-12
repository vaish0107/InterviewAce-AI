package com.interviewace.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.ResumeAnalysisServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeAnalysisServiceImplTests {
    @TempDir Path temporaryDirectory;
    private ResumeRepository resumes;
    private ResumeAnalysisRepository analyses;
    private AuthenticatedUserService authenticatedUsers;
    private AiResumeClient aiClient;
    private ResumeAnalysisService service;
    private User user;

    @BeforeEach void setUp() {
        resumes = mock(ResumeRepository.class); analyses = mock(ResumeAnalysisRepository.class);
        authenticatedUsers = mock(AuthenticatedUserService.class); aiClient = mock(AiResumeClient.class);
        user = new User("Jane", "jane@example.com", "hash"); user.setId(1L);
        when(authenticatedUsers.getAuthenticatedUser()).thenReturn(user);
        when(analyses.saveAndFlush(any())).thenAnswer(invocation -> {
            ResumeAnalysis value = invocation.getArgument(0);
            if (value.getId() == null) value.setId(50L);
            if (value.getCreatedAt() == null) value.setCreatedAt(LocalDateTime.now());
            return value;
        });
        service = new ResumeAnalysisServiceImpl(resumes, analyses, authenticatedUsers, aiClient,
                new JsonListMapper(new ObjectMapper()));
    }

    @Test void persistsAndReturnsSuccessfulAnalysis() throws Exception {
        Resume resume = existingResume(); own(resume);
        when(aiClient.analyzeResume(any())).thenReturn(validResponse(84));
        ResumeAnalysisDto result = service.analyzeResume(10L);
        assertEquals(84, result.atsScore()); assertEquals(AnalysisStatus.COMPLETED, result.status());
        assertEquals(List.of("Java", "PostgreSQL"), result.detectedSkills());
        assertEquals(UploadStatus.COMPLETED, resume.getUploadStatus());
        ArgumentCaptor<ResumeAnalysis> captor = ArgumentCaptor.forClass(ResumeAnalysis.class);
        verify(analyses, times(2)).saveAndFlush(captor.capture());
        assertEquals(AnalysisStatus.COMPLETED, captor.getValue().getAnalysisStatus());
        assertNotNull(captor.getValue().getAnalyzedAt());
    }

    @Test void protectsOwnershipAndHidesForeignResume() {
        when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.analyzeResume(10L));
        verifyNoInteractions(aiClient, analyses);
    }

    @Test void reportsResumeNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> service.getLatestAnalysis(99L));
    }

    @Test void rejectsMissingPhysicalFileBeforeCallingAi() {
        Resume resume = resume(temporaryDirectory.resolve("missing.pdf")); own(resume);
        AiAnalysisException error = assertThrows(AiAnalysisException.class, () -> service.analyzeResume(10L));
        assertEquals("Stored resume file is unavailable", error.getMessage());
        verifyNoInteractions(aiClient, analyses);
    }

    @Test void persistsFailedStatusWhenAiServiceFails() throws Exception {
        Resume resume = existingResume(); own(resume);
        when(aiClient.analyzeResume(any())).thenThrow(new AiServiceUnavailableException("AI analysis service is unavailable"));
        assertThrows(AiServiceUnavailableException.class, () -> service.analyzeResume(10L));
        ArgumentCaptor<ResumeAnalysis> captor = ArgumentCaptor.forClass(ResumeAnalysis.class);
        verify(analyses, times(2)).saveAndFlush(captor.capture());
        ResumeAnalysis failed = captor.getValue();
        assertEquals(AnalysisStatus.FAILED, failed.getAnalysisStatus());
        assertEquals("AI analysis service is unavailable", failed.getFailureMessage());
        assertEquals(UploadStatus.FAILED, resume.getUploadStatus());
    }

    @Test void rejectsOutOfRangeAtsScoreAndPersistsFailure() throws Exception {
        Resume resume = existingResume(); own(resume);
        when(aiClient.analyzeResume(any())).thenReturn(validResponse(101));
        AiAnalysisException error = assertThrows(AiAnalysisException.class, () -> service.analyzeResume(10L));
        assertEquals("AI service returned an invalid ATS score", error.getMessage());
        ArgumentCaptor<ResumeAnalysis> captor = ArgumentCaptor.forClass(ResumeAnalysis.class);
        verify(analyses, times(2)).saveAndFlush(captor.capture());
        assertEquals(AnalysisStatus.FAILED, captor.getValue().getAnalysisStatus());
    }

    @Test void getsLatestPersistedAnalysisForOwner() throws Exception {
        Resume resume = existingResume(); own(resume);
        ResumeAnalysis analysis = new ResumeAnalysis(resume, AnalysisStatus.COMPLETED);
        analysis.setId(51L); analysis.setAtsScore(77); analysis.setGrade("B");
        analysis.setDetectedSkills("[\"Java\"]"); analysis.setStrengths("[]");
        analysis.setWeaknesses("[]"); analysis.setSuggestions("[\"Add metrics\"]");
        when(analyses.findTopByResumeIdOrderByCreatedAtDesc(10L)).thenReturn(Optional.of(analysis));
        ResumeAnalysisDto result = service.getLatestAnalysis(10L);
        assertEquals(51L, result.id()); assertEquals(List.of("Add metrics"), result.recommendations());
    }

    private void own(Resume resume) { when(resumes.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(resume)); }
    private Resume existingResume() throws Exception {
        Path file = temporaryDirectory.resolve("resume.pdf"); Files.writeString(file, "%PDF-1.4"); return resume(file);
    }
    private Resume resume(Path path) {
        Resume value = new Resume(user, "resume.pdf", "stored.pdf", path.toString(), "application/pdf", 8L);
        value.setId(10L); return value;
    }
    private AiResumeAnalysisResponse validResponse(int score) {
        AiSectionScoresDto sections = new AiSectionScoresDto(25, 15, 10, 10, 8, 3, 8);
        return new AiResumeAnalysisResponse("resume.pdf", 1, 1000, score, "A", sections,
                List.of("Java", "PostgreSQL"), List.of("Strong skills"), List.of("No LinkedIn"),
                List.of("Add LinkedIn"), "Heuristic score");
    }
}
