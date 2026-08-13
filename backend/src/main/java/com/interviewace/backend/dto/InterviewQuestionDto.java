package com.interviewace.backend.dto;
import com.interviewace.backend.entity.InterviewDifficulty;
import java.time.LocalDateTime;
public record InterviewQuestionDto(Long id, String externalQuestionId, String questionText,
        String category, String skill, InterviewDifficulty difficulty, Integer questionOrder,
        String answerText, LocalDateTime answeredAt, Boolean adaptive, Long parentQuestionId,
        Integer followUpDepth, String focusArea) {}
