package com.interviewace.backend.service;
import com.interviewace.backend.dto.InterviewCoachingReportDto;
public interface InterviewCoachingService { InterviewCoachingReportDto generateCoaching(Long interviewId); InterviewCoachingReportDto getCoaching(Long interviewId); }
