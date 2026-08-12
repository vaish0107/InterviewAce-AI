package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiAnswerEvaluationResponse(@JsonProperty("overall_score") Integer overallScore,
        @JsonProperty("relevance_score") Integer relevanceScore,
        @JsonProperty("correctness_score") Integer correctnessScore,
        @JsonProperty("completeness_score") Integer completenessScore,
        @JsonProperty("communication_score") Integer communicationScore,
        List<String> strengths, List<String> weaknesses,
        @JsonProperty("missing_key_points") List<String> missingKeyPoints,
        @JsonProperty("improved_answer") String improvedAnswer, String summary,
        @JsonProperty("evaluation_note") String evaluationNote) {}
