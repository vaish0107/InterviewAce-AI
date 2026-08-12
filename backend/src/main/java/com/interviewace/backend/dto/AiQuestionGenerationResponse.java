package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiQuestionGenerationResponse(@JsonProperty("interview_type") String interviewType,
        String difficulty, @JsonProperty("total_questions") Integer totalQuestions,
        List<AiInterviewQuestionDto> questions) {}
