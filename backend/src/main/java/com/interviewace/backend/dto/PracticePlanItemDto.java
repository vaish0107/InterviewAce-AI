package com.interviewace.backend.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
public record PracticePlanItemDto(Integer order,String activity,String focus,@JsonAlias("suggested_question_count") Integer suggestedQuestionCount){}
