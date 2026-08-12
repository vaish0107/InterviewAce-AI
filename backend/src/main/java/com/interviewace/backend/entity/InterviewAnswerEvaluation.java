package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_answer_evaluations", uniqueConstraints = @UniqueConstraint(name = "uk_answer_evaluation_question", columnNames = "question_id"))
public class InterviewAnswerEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "question_id", nullable = false) private InterviewQuestionRecord question;
    @Column(name = "overall_score") private Integer overallScore;
    @Column(name = "relevance_score") private Integer relevanceScore;
    @Column(name = "correctness_score") private Integer correctnessScore;
    @Column(name = "completeness_score") private Integer completenessScore;
    @Column(name = "communication_score") private Integer communicationScore;
    @Column(columnDefinition = "TEXT") private String strengths;
    @Column(columnDefinition = "TEXT") private String weaknesses;
    @Column(name = "missing_key_points", columnDefinition = "TEXT") private String missingKeyPoints;
    @Column(name = "improved_answer", columnDefinition = "TEXT") private String improvedAnswer;
    @Column(columnDefinition = "TEXT") private String summary;
    @Column(name = "evaluation_note", columnDefinition = "TEXT") private String evaluationNote;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EvaluationStatus status;
    @Column(name = "failure_message", columnDefinition = "TEXT") private String failureMessage;
    @Column(name = "evaluated_at") private LocalDateTime evaluatedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    public InterviewAnswerEvaluation() {}
    public InterviewAnswerEvaluation(InterviewQuestionRecord question, EvaluationStatus status) { this.question = question; this.status = status; }
    @PrePersist void prePersist() { LocalDateTime now = LocalDateTime.now(); createdAt = now; updatedAt = now; if (status == null) status = EvaluationStatus.PENDING; }
    @PreUpdate void preUpdate() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public InterviewQuestionRecord getQuestion() { return question; } public void setQuestion(InterviewQuestionRecord value) { question = value; }
    public Integer getOverallScore() { return overallScore; } public void setOverallScore(Integer value) { overallScore = value; }
    public Integer getRelevanceScore() { return relevanceScore; } public void setRelevanceScore(Integer value) { relevanceScore = value; }
    public Integer getCorrectnessScore() { return correctnessScore; } public void setCorrectnessScore(Integer value) { correctnessScore = value; }
    public Integer getCompletenessScore() { return completenessScore; } public void setCompletenessScore(Integer value) { completenessScore = value; }
    public Integer getCommunicationScore() { return communicationScore; } public void setCommunicationScore(Integer value) { communicationScore = value; }
    public String getStrengths() { return strengths; } public void setStrengths(String value) { strengths = value; }
    public String getWeaknesses() { return weaknesses; } public void setWeaknesses(String value) { weaknesses = value; }
    public String getMissingKeyPoints() { return missingKeyPoints; } public void setMissingKeyPoints(String value) { missingKeyPoints = value; }
    public String getImprovedAnswer() { return improvedAnswer; } public void setImprovedAnswer(String value) { improvedAnswer = value; }
    public String getSummary() { return summary; } public void setSummary(String value) { summary = value; }
    public String getEvaluationNote() { return evaluationNote; } public void setEvaluationNote(String value) { evaluationNote = value; }
    public EvaluationStatus getStatus() { return status; } public void setStatus(EvaluationStatus value) { status = value; }
    public String getFailureMessage() { return failureMessage; } public void setFailureMessage(String value) { failureMessage = value; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; } public void setEvaluatedAt(LocalDateTime value) { evaluatedAt = value; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
