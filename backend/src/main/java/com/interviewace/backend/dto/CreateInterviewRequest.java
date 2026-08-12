package com.interviewace.backend.dto;
import com.interviewace.backend.entity.InterviewDifficulty;
import com.interviewace.backend.entity.InterviewType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record CreateInterviewRequest(Long resumeId,
        @NotNull(message = "Interview type is required") InterviewType interviewType,
        @NotNull(message = "Difficulty is required") InterviewDifficulty difficulty,
        @NotNull(message = "Question count is required") @Min(value = 3, message = "Question count must be at least 3")
        @Max(value = 20, message = "Question count must not exceed 20") Integer questionCount) {}
