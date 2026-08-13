package com.interviewace.backend.dto;
public record FollowUpGenerationDto(boolean created, InterviewQuestionDto question, String reason) {}
