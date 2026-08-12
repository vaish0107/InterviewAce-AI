package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_match_analyses")
public class JobMatchAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "resume_id", nullable = false) private Resume resume;
    @Column(name = "job_description", nullable = false, columnDefinition = "TEXT") private String jobDescription;
    @Column(name = "match_percentage", nullable = false) private Integer matchPercentage;
    @Column(name = "resume_skill_count", nullable = false) private Integer resumeSkillCount;
    @Column(name = "job_skill_count", nullable = false) private Integer jobSkillCount;
    @Column(name = "matched_skills", nullable = false, columnDefinition = "TEXT") private String matchedSkills;
    @Column(name = "missing_skills", nullable = false, columnDefinition = "TEXT") private String missingSkills;
    @Column(name = "additional_resume_skills", nullable = false, columnDefinition = "TEXT") private String additionalResumeSkills;
    @Column(name = "category_matches", nullable = false, columnDefinition = "TEXT") private String categoryMatches;
    @Column(nullable = false, columnDefinition = "TEXT") private String strengths;
    @Column(nullable = false, columnDefinition = "TEXT") private String recommendations;
    @Column(name = "matching_note", nullable = false, columnDefinition = "TEXT") private String matchingNote;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public JobMatchAnalysis() {}
    public JobMatchAnalysis(Resume resume, String jobDescription) { this.resume = resume; this.jobDescription = jobDescription; }
    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public Resume getResume() { return resume; } public void setResume(Resume value) { resume = value; }
    public String getJobDescription() { return jobDescription; } public void setJobDescription(String value) { jobDescription = value; }
    public Integer getMatchPercentage() { return matchPercentage; } public void setMatchPercentage(Integer value) { matchPercentage = value; }
    public Integer getResumeSkillCount() { return resumeSkillCount; } public void setResumeSkillCount(Integer value) { resumeSkillCount = value; }
    public Integer getJobSkillCount() { return jobSkillCount; } public void setJobSkillCount(Integer value) { jobSkillCount = value; }
    public String getMatchedSkills() { return matchedSkills; } public void setMatchedSkills(String value) { matchedSkills = value; }
    public String getMissingSkills() { return missingSkills; } public void setMissingSkills(String value) { missingSkills = value; }
    public String getAdditionalResumeSkills() { return additionalResumeSkills; } public void setAdditionalResumeSkills(String value) { additionalResumeSkills = value; }
    public String getCategoryMatches() { return categoryMatches; } public void setCategoryMatches(String value) { categoryMatches = value; }
    public String getStrengths() { return strengths; } public void setStrengths(String value) { strengths = value; }
    public String getRecommendations() { return recommendations; } public void setRecommendations(String value) { recommendations = value; }
    public String getMatchingNote() { return matchingNote; } public void setMatchingNote(String value) { matchingNote = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
