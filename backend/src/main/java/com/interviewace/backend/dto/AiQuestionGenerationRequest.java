package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiQuestionGenerationRequest(List<String> skills,
        @JsonProperty("interview_type") String interviewType, String difficulty,
        @JsonProperty("question_count") Integer questionCount) {}
