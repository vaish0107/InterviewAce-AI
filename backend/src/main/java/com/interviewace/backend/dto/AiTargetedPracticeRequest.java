package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiTargetedPracticeRequest(@JsonProperty("focus_area") String focusArea, String skill, String difficulty, @JsonProperty("question_count") Integer questionCount, @JsonProperty("weakness_context") String weaknessContext, @JsonProperty("avoid_questions") List<String> avoidQuestions) {}
