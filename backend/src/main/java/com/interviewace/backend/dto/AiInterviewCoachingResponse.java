package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiInterviewCoachingResponse(String summary,@JsonProperty("primary_focus_areas") List<CoachingFocusAreaDto> primaryFocusAreas,
 @JsonProperty("practice_recommendations") List<String> practiceRecommendations,@JsonProperty("revision_topics") List<String> revisionTopics,
 @JsonProperty("communication_tips") List<String> communicationTips,@JsonProperty("next_practice_plan") List<PracticePlanItemDto> nextPracticePlan,
 @JsonProperty("coaching_note") String coachingNote){}
