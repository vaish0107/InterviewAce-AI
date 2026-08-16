package com.interviewace.backend.dto;
import com.interviewace.backend.entity.InterviewDifficulty;
public record CreateTargetedPracticeRequest(Long sourceInterviewId, String focusArea, String skill, InterviewDifficulty difficulty, Integer questionCount, String weaknessContext) {}
