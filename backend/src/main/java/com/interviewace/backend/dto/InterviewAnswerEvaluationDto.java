package com.interviewace.backend.dto;
import com.interviewace.backend.entity.EvaluationStatus;
import java.time.LocalDateTime;
import java.util.List;
public record InterviewAnswerEvaluationDto(Long id, Long questionId, Integer overallScore,
        Integer relevanceScore, Integer correctnessScore, Integer completenessScore,
        Integer communicationScore, List<String> strengths, List<String> weaknesses,
        List<String> missingKeyPoints, String improvedAnswer, String summary,
        String evaluationNote, EvaluationStatus status, LocalDateTime evaluatedAt,
        String failureMessage) {}
