package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.DashboardSummaryDto;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.AuthenticatedUserService;
import com.interviewace.backend.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final ResumeRepository resumes;
    private final ResumeAnalysisRepository analyses;
    private final JobMatchAnalysisRepository jobMatches;
    private final InterviewSessionRepository interviews;
    private final InterviewAnswerEvaluationRepository evaluations;
    private final AuthenticatedUserService authenticatedUsers;

    public DashboardServiceImpl(ResumeRepository resumes, ResumeAnalysisRepository analyses,
            JobMatchAnalysisRepository jobMatches, InterviewSessionRepository interviews,
            InterviewAnswerEvaluationRepository evaluations, AuthenticatedUserService authenticatedUsers) {
        this.resumes = resumes; this.analyses = analyses; this.jobMatches = jobMatches;
        this.interviews = interviews; this.evaluations = evaluations; this.authenticatedUsers = authenticatedUsers;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getMySummary() {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        List<InterviewAnswerEvaluation> completed = evaluations.findByUserIdAndStatus(userId, EvaluationStatus.COMPLETED);
        Double interviewAverage = completed.isEmpty() ? null
                : Math.round(completed.stream().mapToInt(InterviewAnswerEvaluation::getOverallScore).average().orElseThrow() * 10.0) / 10.0;
        Integer latestAts = analyses.findTopByResumeUserIdAndAnalysisStatusOrderByAnalyzedAtDesc(userId, AnalysisStatus.COMPLETED)
                .map(ResumeAnalysis::getAtsScore).orElse(null);
        Double latestMatch = jobMatches.findTopByResumeUserIdOrderByCreatedAtDesc(userId)
                .map(value -> value.getMatchPercentage().doubleValue()).orElse(null);
        return new DashboardSummaryDto(Math.toIntExact(resumes.countByUserId(userId)),
                Math.toIntExact(analyses.countByResumeUserIdAndAnalysisStatus(userId, AnalysisStatus.COMPLETED)),
                Math.toIntExact(jobMatches.countByResumeUserId(userId)), Math.toIntExact(interviews.countByUserId(userId)),
                Math.toIntExact(interviews.countByUserIdAndStatus(userId, InterviewSessionStatus.COMPLETED)), completed.size(),
                latestAts, latestMatch, interviewAverage);
    }
}
