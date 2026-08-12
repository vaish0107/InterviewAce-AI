package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Column(name = "original_file_name", nullable = false, length = 255) private String originalFileName;
    @Column(name = "stored_file_name", nullable = false, length = 255) private String storedFileName;
    @Column(name = "file_path", nullable = false, length = 500) private String filePath;
    @Column(name = "file_type", nullable = false, length = 50) private String fileType;
    @Column(name = "file_size", nullable = false) private Long fileSize;
    @Column(name = "extracted_text", columnDefinition = "TEXT") private String extractedText;
    @Enumerated(EnumType.STRING) @Column(name = "upload_status", nullable = false, length = 20) private UploadStatus uploadStatus = UploadStatus.UPLOADED;
    @Column(name = "uploaded_at", nullable = false, updatable = false) private LocalDateTime uploadedAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public Resume() {}
    public Resume(User user, String originalFileName, String storedFileName, String filePath, String fileType, Long fileSize) {
        this.user = user; this.originalFileName = originalFileName; this.storedFileName = storedFileName;
        this.filePath = filePath; this.fileType = fileType; this.fileSize = fileSize;
    }
    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); uploadedAt = now; updatedAt = now; if (uploadStatus == null) uploadStatus = UploadStatus.UPLOADED; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
    public String getOriginalFileName() { return originalFileName; } public void setOriginalFileName(String value) { this.originalFileName = value; }
    public String getStoredFileName() { return storedFileName; } public void setStoredFileName(String value) { this.storedFileName = value; }
    public String getFilePath() { return filePath; } public void setFilePath(String value) { this.filePath = value; }
    public String getFileType() { return fileType; } public void setFileType(String value) { this.fileType = value; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long value) { this.fileSize = value; }
    public String getExtractedText() { return extractedText; } public void setExtractedText(String value) { this.extractedText = value; }
    public UploadStatus getUploadStatus() { return uploadStatus; } public void setUploadStatus(UploadStatus value) { this.uploadStatus = value; }
    public LocalDateTime getUploadedAt() { return uploadedAt; } public void setUploadedAt(LocalDateTime value) { this.uploadedAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
