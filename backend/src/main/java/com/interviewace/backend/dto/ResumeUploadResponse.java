package com.interviewace.backend.dto;

import com.interviewace.backend.entity.UploadStatus;
import java.time.LocalDateTime;

public record ResumeUploadResponse(Long id, String originalFileName, Long fileSize,
                                   UploadStatus uploadStatus, String message, LocalDateTime uploadedAt) {}
