package com.interviewace.backend.dto;
import com.interviewace.backend.entity.*;
import java.time.LocalDateTime;
import java.util.List;
public record InterviewSessionDto(Long id, Long resumeId, String resumeFileName,
        InterviewType interviewType, InterviewDifficulty difficulty, Integer totalQuestions,
        List<String> detectedSkills, InterviewSessionStatus status, Integer answeredQuestions,
        LocalDateTime startedAt, LocalDateTime completedAt, LocalDateTime createdAt,
        LocalDateTime updatedAt, List<InterviewQuestionDto> questions, InterviewSessionMode sessionMode,
        String targetSkill, String targetFocusArea, Long sourceInterviewId) {}
