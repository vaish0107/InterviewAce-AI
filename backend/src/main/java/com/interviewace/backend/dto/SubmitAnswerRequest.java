package com.interviewace.backend.dto;
import jakarta.validation.constraints.NotBlank;
public record SubmitAnswerRequest(@NotBlank(message = "Answer is required") String answer) {}
