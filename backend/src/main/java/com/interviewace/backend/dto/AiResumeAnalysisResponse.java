package com.interviewace.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiResumeAnalysisResponse(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("page_count") Integer pageCount,
        @JsonProperty("character_count") Integer characterCount,
        @JsonProperty("ats_score") Integer atsScore,
        String grade,
        @JsonProperty("section_scores") AiSectionScoresDto sectionScores,
        @JsonProperty("detected_skills") List<String> detectedSkills,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations,
        @JsonProperty("scoring_note") String scoringNote) {}
