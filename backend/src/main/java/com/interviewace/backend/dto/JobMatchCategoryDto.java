package com.interviewace.backend.dto;
import java.util.List;
public record JobMatchCategoryDto(List<String> requiredSkills, List<String> matchedSkills,
        List<String> missingSkills, Integer matchPercentage) {}
