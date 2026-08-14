package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
public record CoachingFocusAreaDto(String title,String reason,String priority,@JsonAlias("related_skills") List<String> relatedSkills){}
