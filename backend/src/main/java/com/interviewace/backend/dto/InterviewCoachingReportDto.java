package com.interviewace.backend.dto;
import com.interviewace.backend.entity.CoachingStatus;
import java.time.LocalDateTime; import java.util.List;
public record InterviewCoachingReportDto(Long id,Long interviewId,String summary,List<CoachingFocusAreaDto> primaryFocusAreas,
 List<String> practiceRecommendations,List<String> revisionTopics,List<String> communicationTips,List<PracticePlanItemDto> nextPracticePlan,
 String coachingNote,CoachingStatus status,String failureMessage,LocalDateTime generatedAt,LocalDateTime createdAt,LocalDateTime updatedAt){}
