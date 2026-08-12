package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
public record AiJobMatchResponse(@JsonProperty("match_percentage") Integer matchPercentage,
        @JsonProperty("resume_skill_count") Integer resumeSkillCount,
        @JsonProperty("job_skill_count") Integer jobSkillCount,
        @JsonProperty("matched_skills") List<String> matchedSkills,
        @JsonProperty("missing_skills") List<String> missingSkills,
        @JsonProperty("additional_resume_skills") List<String> additionalResumeSkills,
        @JsonProperty("category_matches") Map<String, AiCategoryMatchDto> categoryMatches,
        List<String> strengths, List<String> recommendations,
        @JsonProperty("matching_note") String matchingNote) {}
