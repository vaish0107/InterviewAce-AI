package com.interviewace.backend.dto;
public record TargetedPracticeSummaryDto(Long sessionId, String focusArea, String skill, Integer questionCount, Integer answeredCount, Integer evaluatedCount, Double averageScore, Double baselineScore, Double scoreChange, RubricAveragesDto rubricAverages, RubricAveragesDto baselineRubricAverages, Long sourceInterviewId) {}
