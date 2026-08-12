package com.interviewace.backend.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record JobMatchRequest(
        @NotBlank(message = "Job description is required")
        @Size(min = 20, message = "Job description must contain at least 20 characters")
        String jobDescription) {}
