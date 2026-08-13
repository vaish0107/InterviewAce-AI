package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiFollowUpRequest(@JsonProperty("original_question") String originalQuestion,
        @JsonProperty("candidate_answer") String candidateAnswer, String category,
        String skill, String difficulty, @JsonProperty("previous_followups") List<String> previousFollowups,
        @JsonProperty("recent_context") List<AiInterviewContextItem> recentContext) {}
