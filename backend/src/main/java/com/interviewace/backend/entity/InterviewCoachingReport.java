package com.interviewace.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="interview_coaching_reports")
public class InterviewCoachingReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="session_id",nullable=false) private InterviewSession session;
 @Column(columnDefinition="TEXT") private String summary;
 @Column(name="primary_focus_areas",columnDefinition="TEXT") private String primaryFocusAreas;
 @Column(name="practice_recommendations",columnDefinition="TEXT") private String practiceRecommendations;
 @Column(name="revision_topics",columnDefinition="TEXT") private String revisionTopics;
 @Column(name="communication_tips",columnDefinition="TEXT") private String communicationTips;
 @Column(name="next_practice_plan",columnDefinition="TEXT") private String nextPracticePlan;
 @Column(name="coaching_note",columnDefinition="TEXT") private String coachingNote;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private CoachingStatus status;
 @Column(name="failure_message",columnDefinition="TEXT") private String failureMessage;
 @Column(name="generated_at") private LocalDateTime generatedAt;
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 public InterviewCoachingReport(){} public InterviewCoachingReport(InterviewSession s){session=s;status=CoachingStatus.PENDING;}
 @PrePersist void create(){createdAt=updatedAt=LocalDateTime.now();} @PreUpdate void update(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;} public InterviewSession getSession(){return session;} public void setSession(InterviewSession v){session=v;}
 public String getSummary(){return summary;} public void setSummary(String v){summary=v;} public String getPrimaryFocusAreas(){return primaryFocusAreas;} public void setPrimaryFocusAreas(String v){primaryFocusAreas=v;}
 public String getPracticeRecommendations(){return practiceRecommendations;} public void setPracticeRecommendations(String v){practiceRecommendations=v;} public String getRevisionTopics(){return revisionTopics;} public void setRevisionTopics(String v){revisionTopics=v;}
 public String getCommunicationTips(){return communicationTips;} public void setCommunicationTips(String v){communicationTips=v;} public String getNextPracticePlan(){return nextPracticePlan;} public void setNextPracticePlan(String v){nextPracticePlan=v;}
 public String getCoachingNote(){return coachingNote;} public void setCoachingNote(String v){coachingNote=v;} public CoachingStatus getStatus(){return status;} public void setStatus(CoachingStatus v){status=v;}
 public String getFailureMessage(){return failureMessage;} public void setFailureMessage(String v){failureMessage=v;} public LocalDateTime getGeneratedAt(){return generatedAt;} public void setGeneratedAt(LocalDateTime v){generatedAt=v;}
 public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;} public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
