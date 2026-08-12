package com.interviewace.backend.dto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
public record JobMatchAnalysisDto(Long id, Long resumeId, Integer matchPercentage,
        Integer resumeSkillCount, Integer jobSkillCount, List<String> matchedSkills,
        List<String> missingSkills, List<String> additionalResumeSkills,
        Map<String, JobMatchCategoryDto> categoryMatches, List<String> strengths,
        List<String> recommendations, String matchingNote, LocalDateTime createdAt) {}
