package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
public record AiJobMatchRequest(@JsonProperty("resume_text") String resumeText,
        @JsonProperty("job_description") String jobDescription) {}
