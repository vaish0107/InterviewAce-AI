package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
public class InterviewSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resume_id") private Resume resume;
    @Enumerated(EnumType.STRING) @Column(name = "interview_type", nullable = false, length = 20) private InterviewType interviewType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InterviewDifficulty difficulty;
    @Column(name = "total_questions", nullable = false) private Integer totalQuestions;
    @Column(name = "detected_skills", nullable = false, columnDefinition = "TEXT") private String detectedSkills;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InterviewSessionStatus status;
    @Column(name = "started_at") private LocalDateTime startedAt;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public InterviewSession() {}
    public InterviewSession(User user, Resume resume, InterviewType interviewType, InterviewDifficulty difficulty, Integer totalQuestions, String detectedSkills) {
        this.user = user; this.resume = resume; this.interviewType = interviewType; this.difficulty = difficulty;
        this.totalQuestions = totalQuestions; this.detectedSkills = detectedSkills; this.status = InterviewSessionStatus.CREATED;
    }
    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; if (status == null) status = InterviewSessionStatus.CREATED; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public User getUser() { return user; } public void setUser(User value) { user = value; }
    public Resume getResume() { return resume; } public void setResume(Resume value) { resume = value; }
    public InterviewType getInterviewType() { return interviewType; } public void setInterviewType(InterviewType value) { interviewType = value; }
    public InterviewDifficulty getDifficulty() { return difficulty; } public void setDifficulty(InterviewDifficulty value) { difficulty = value; }
    public Integer getTotalQuestions() { return totalQuestions; } public void setTotalQuestions(Integer value) { totalQuestions = value; }
    public String getDetectedSkills() { return detectedSkills; } public void setDetectedSkills(String value) { detectedSkills = value; }
    public InterviewSessionStatus getStatus() { return status; } public void setStatus(InterviewSessionStatus value) { status = value; }
    public LocalDateTime getStartedAt() { return startedAt; } public void setStartedAt(LocalDateTime value) { startedAt = value; }
    public LocalDateTime getCompletedAt() { return completedAt; } public void setCompletedAt(LocalDateTime value) { completedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
