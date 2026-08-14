package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiCoachingAnswerItem(String question,String answer,String category,String skill,boolean adaptive,
 @JsonProperty("overall_score") Integer overallScore,@JsonProperty("relevance_score") Integer relevanceScore,
 @JsonProperty("correctness_score") Integer correctnessScore,@JsonProperty("completeness_score") Integer completenessScore,
 @JsonProperty("communication_score") Integer communicationScore,List<String> weaknesses,
 @JsonProperty("missing_key_points") List<String> missingKeyPoints){}
