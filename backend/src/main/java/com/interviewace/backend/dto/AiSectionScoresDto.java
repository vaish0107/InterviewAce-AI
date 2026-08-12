package com.interviewace.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiSectionScoresDto(
        @JsonProperty("technical_skills") Integer technicalSkills,
        Integer projects,
        Integer education,
        @JsonProperty("contact_information") Integer contactInformation,
        Integer experience,
        Integer certifications,
        Integer completeness) {}
