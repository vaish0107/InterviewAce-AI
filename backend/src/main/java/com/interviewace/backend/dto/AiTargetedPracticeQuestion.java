package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record AiTargetedPracticeQuestion(String question, String category, String skill, String difficulty, @JsonProperty("focus_concept") String focusConcept) {}
