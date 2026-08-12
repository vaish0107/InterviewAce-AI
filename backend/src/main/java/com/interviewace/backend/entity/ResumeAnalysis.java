package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "resume_id", nullable = false) private Resume resume;
    @Column(name = "ats_score") private Integer atsScore;
    @Column(length = 10) private String grade;
    @Column(name = "overall_summary", columnDefinition = "TEXT") private String overallSummary;
    @Column(columnDefinition = "TEXT") private String strengths;
    @Column(columnDefinition = "TEXT") private String weaknesses;
    @Column(columnDefinition = "TEXT") private String suggestions;
    @Column(name = "detected_skills", columnDefinition = "TEXT") private String detectedSkills;
    @Column(name = "missing_keywords", columnDefinition = "TEXT") private String missingKeywords;
    @Column(name = "scoring_note", columnDefinition = "TEXT") private String scoringNote;
    @Enumerated(EnumType.STRING) @Column(name = "analysis_status", nullable = false, length = 20) private AnalysisStatus analysisStatus;
    @Column(name = "failure_message", columnDefinition = "TEXT") private String failureMessage;
    @Column(name = "analyzed_at") private LocalDateTime analyzedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public ResumeAnalysis() {}
    public ResumeAnalysis(Resume resume, AnalysisStatus analysisStatus) { this.resume = resume; this.analysisStatus = analysisStatus; }
    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; if (analysisStatus == null) analysisStatus = AnalysisStatus.PENDING; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Resume getResume() { return resume; } public void setResume(Resume value) { resume = value; }
    public Integer getAtsScore() { return atsScore; } public void setAtsScore(Integer value) { atsScore = value; }
    public String getGrade() { return grade; } public void setGrade(String value) { grade = value; }
    public String getOverallSummary() { return overallSummary; } public void setOverallSummary(String value) { overallSummary = value; }
    public String getStrengths() { return strengths; } public void setStrengths(String value) { strengths = value; }
    public String getWeaknesses() { return weaknesses; } public void setWeaknesses(String value) { weaknesses = value; }
    public String getSuggestions() { return suggestions; } public void setSuggestions(String value) { suggestions = value; }
    public String getDetectedSkills() { return detectedSkills; } public void setDetectedSkills(String value) { detectedSkills = value; }
    public String getMissingKeywords() { return missingKeywords; } public void setMissingKeywords(String value) { missingKeywords = value; }
    public String getScoringNote() { return scoringNote; } public void setScoringNote(String value) { scoringNote = value; }
    public AnalysisStatus getAnalysisStatus() { return analysisStatus; } public void setAnalysisStatus(AnalysisStatus value) { analysisStatus = value; }
    public String getFailureMessage() { return failureMessage; } public void setFailureMessage(String value) { failureMessage = value; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; } public void setAnalyzedAt(LocalDateTime value) { analyzedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
