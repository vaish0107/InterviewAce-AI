package com.interviewace.backend.dto;
import com.interviewace.backend.entity.UploadStatus;
import java.time.LocalDateTime;
public record ResumeDto(Long id, String originalFileName, String fileType, Long fileSize,
                        UploadStatus uploadStatus, LocalDateTime uploadedAt, LocalDateTime updatedAt) {}
