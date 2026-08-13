package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record AiFollowUpResponse(@JsonProperty("should_ask_followup") Boolean shouldAskFollowup,
        String question, String reason, @JsonProperty("focus_area") String focusArea) {}
