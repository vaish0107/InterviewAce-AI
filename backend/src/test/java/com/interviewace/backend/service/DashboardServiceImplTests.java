package com.interviewace.backend.service;

import com.interviewace.backend.dto.DashboardSummaryDto;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceImplTests {
    private ResumeRepository resumes; private ResumeAnalysisRepository analyses; private JobMatchAnalysisRepository matches;
    private InterviewSessionRepository interviews; private InterviewAnswerEvaluationRepository evaluations;
    private DashboardServiceImpl service;

    @BeforeEach void setup() {
        resumes = mock(ResumeRepository.class); analyses = mock(ResumeAnalysisRepository.class); matches = mock(JobMatchAnalysisRepository.class);
        interviews = mock(InterviewSessionRepository.class); evaluations = mock(InterviewAnswerEvaluationRepository.class);
        AuthenticatedUserService users = mock(AuthenticatedUserService.class); User user = new User(); user.setId(5L);
        when(users.getAuthenticatedUser()).thenReturn(user);
        service = new DashboardServiceImpl(resumes, analyses, matches, interviews, evaluations, users);
    }

    @Test void emptyDashboardReturnsZerosAndNulls() {
        when(evaluations.findByUserIdAndStatus(5L, EvaluationStatus.COMPLETED)).thenReturn(List.of());
        DashboardSummaryDto result = service.getMySummary();
        assertEquals(0, result.resumeCount()); assertEquals(0, result.completedResumeAnalyses()); assertEquals(0, result.jobMatchCount());
        assertEquals(0, result.interviewCount()); assertEquals(0, result.completedInterviewCount()); assertEquals(0, result.evaluatedAnswerCount());
        assertNull(result.latestAtsScore()); assertNull(result.latestJobMatchPercentage()); assertNull(result.interviewAverageScore());
    }

    @Test void dashboardReturnsRealCountsLatestValuesAndCompletedEvaluationAverage() {
        when(resumes.countByUserId(5L)).thenReturn(3L); when(analyses.countByResumeUserIdAndAnalysisStatus(5L, AnalysisStatus.COMPLETED)).thenReturn(2L);
        when(matches.countByResumeUserId(5L)).thenReturn(4L); when(interviews.countByUserId(5L)).thenReturn(6L);
        when(interviews.countByUserIdAndStatus(5L, InterviewSessionStatus.COMPLETED)).thenReturn(5L);
        ResumeAnalysis analysis = new ResumeAnalysis(); analysis.setAtsScore(87);
        when(analyses.findTopByResumeUserIdAndAnalysisStatusOrderByAnalyzedAtDesc(5L, AnalysisStatus.COMPLETED)).thenReturn(Optional.of(analysis));
        JobMatchAnalysis match = new JobMatchAnalysis(); match.setMatchPercentage(76);
        when(matches.findTopByResumeUserIdOrderByCreatedAtDesc(5L)).thenReturn(Optional.of(match));
        InterviewAnswerEvaluation first = evaluation(80); InterviewAnswerEvaluation second = evaluation(85);
        when(evaluations.findByUserIdAndStatus(5L, EvaluationStatus.COMPLETED)).thenReturn(List.of(first, second));
        DashboardSummaryDto result = service.getMySummary();
        assertEquals(3, result.resumeCount()); assertEquals(2, result.completedResumeAnalyses()); assertEquals(4, result.jobMatchCount());
        assertEquals(5, result.completedInterviewCount()); assertEquals(2, result.evaluatedAnswerCount());
        assertEquals(87, result.latestAtsScore()); assertEquals(76.0, result.latestJobMatchPercentage()); assertEquals(82.5, result.interviewAverageScore());
    }
    private InterviewAnswerEvaluation evaluation(int score) { InterviewAnswerEvaluation value = new InterviewAnswerEvaluation(); value.setOverallScore(score); return value; }
}
