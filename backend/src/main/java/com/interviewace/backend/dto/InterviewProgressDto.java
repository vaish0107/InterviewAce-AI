package com.interviewace.backend.dto;

import java.util.List;
import java.util.Map;

public record InterviewProgressDto(Integer totalInterviews, Integer completedInterviews, Integer inProgressInterviews,
        Integer totalQuestionsAnswered, Integer totalEvaluatedAnswers, Double overallAverageScore,
        Double averageRelevance, Double averageCorrectness, Double averageCompleteness, Double averageCommunication,
        Map<String, Double> categoryAverages, Map<String, Double> skillAverages,
        List<InterviewTrendPointDto> scoreTrend, String strongestCategory, String weakestCategory) {}
