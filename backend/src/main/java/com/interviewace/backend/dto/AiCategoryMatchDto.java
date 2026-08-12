package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiCategoryMatchDto(@JsonProperty("required_skills") List<String> requiredSkills,
        @JsonProperty("matched_skills") List<String> matchedSkills,
        @JsonProperty("missing_skills") List<String> missingSkills,
        @JsonProperty("match_percentage") Integer matchPercentage) {}
