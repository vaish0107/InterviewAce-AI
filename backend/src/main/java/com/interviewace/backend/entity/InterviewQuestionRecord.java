package com.interviewace.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_questions", uniqueConstraints = @UniqueConstraint(name = "uk_interview_question_order", columnNames = {"session_id", "question_order"}))
public class InterviewQuestionRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id", nullable = false) private InterviewSession session;
    @Column(name = "external_question_id", nullable = false, length = 64) private String externalQuestionId;
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT") private String questionText;
    @Column(nullable = false, length = 30) private String category;
    @Column(length = 100) private String skill;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InterviewDifficulty difficulty;
    @Column(name = "question_order", nullable = false) private Integer questionOrder;
    @Column(name = "answer_text", columnDefinition = "TEXT") private String answerText;
    @Column(name = "answered_at") private LocalDateTime answeredAt;
    @Column(nullable = false, columnDefinition = "boolean default false") private Boolean adaptive = false;
    @Column(name = "parent_question_id") private Long parentQuestionId;
    @Column(name = "follow_up_depth", nullable = false, columnDefinition = "integer default 0") private Integer followUpDepth = 0;
    @Column(name = "generation_reason", length = 500) private String generationReason;
    @Column(name = "focus_area", length = 200) private String focusArea;
    public InterviewQuestionRecord() {}
    public InterviewQuestionRecord(InterviewSession session, String externalQuestionId, String questionText, String category, String skill, InterviewDifficulty difficulty, Integer questionOrder) {
        this.session = session; this.externalQuestionId = externalQuestionId; this.questionText = questionText;
        this.category = category; this.skill = skill; this.difficulty = difficulty; this.questionOrder = questionOrder;
    }
    public Long getId() { return id; } public void setId(Long value) { id = value; }
    public InterviewSession getSession() { return session; } public void setSession(InterviewSession value) { session = value; }
    public String getExternalQuestionId() { return externalQuestionId; } public void setExternalQuestionId(String value) { externalQuestionId = value; }
    public String getQuestionText() { return questionText; } public void setQuestionText(String value) { questionText = value; }
    public String getCategory() { return category; } public void setCategory(String value) { category = value; }
    public String getSkill() { return skill; } public void setSkill(String value) { skill = value; }
    public InterviewDifficulty getDifficulty() { return difficulty; } public void setDifficulty(InterviewDifficulty value) { difficulty = value; }
    public Integer getQuestionOrder() { return questionOrder; } public void setQuestionOrder(Integer value) { questionOrder = value; }
    public String getAnswerText() { return answerText; } public void setAnswerText(String value) { answerText = value; }
    public LocalDateTime getAnsweredAt() { return answeredAt; } public void setAnsweredAt(LocalDateTime value) { answeredAt = value; }
    public Boolean getAdaptive() { return adaptive; } public void setAdaptive(Boolean value) { adaptive = value; }
    public Long getParentQuestionId() { return parentQuestionId; } public void setParentQuestionId(Long value) { parentQuestionId = value; }
    public Integer getFollowUpDepth() { return followUpDepth; } public void setFollowUpDepth(Integer value) { followUpDepth = value; }
    public String getGenerationReason() { return generationReason; } public void setGenerationReason(String value) { generationReason = value; }
    public String getFocusArea() { return focusArea; } public void setFocusArea(String value) { focusArea = value; }
}
