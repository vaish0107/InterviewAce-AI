package com.interviewace.backend.dto;

public record DashboardSummaryDto(Integer resumeCount, Integer completedResumeAnalyses, Integer jobMatchCount,
        Integer interviewCount, Integer completedInterviewCount, Integer evaluatedAnswerCount,
        Integer latestAtsScore, Double latestJobMatchPercentage, Double interviewAverageScore) {}
