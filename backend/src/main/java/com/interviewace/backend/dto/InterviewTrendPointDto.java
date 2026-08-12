package com.interviewace.backend.dto;

import java.time.LocalDateTime;

public record InterviewTrendPointDto(Long interviewId, LocalDateTime completedAt, Double averageScore) {}
