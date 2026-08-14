package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
public record AiInterviewCoachingRequest(@JsonProperty("interview_type") String interviewType,String difficulty,List<AiCoachingAnswerItem> answers){}
