package com.interviewace.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record InterviewSummaryDto(Long interviewId, String interviewType, String difficulty, String status,
        Integer totalQuestions, Integer answeredQuestions, Integer evaluatedQuestions, Integer unansweredQuestions,
        Double averageScore, Double averageRelevance, Double averageCorrectness, Double averageCompleteness,
        Double averageCommunication, Map<String, Double> categoryScores, Map<String, Double> skillScores,
        String strongestCategory, String weakestCategory, LocalDateTime startedAt, LocalDateTime completedAt) {}
