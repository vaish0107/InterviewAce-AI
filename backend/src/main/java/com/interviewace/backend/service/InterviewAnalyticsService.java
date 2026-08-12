package com.interviewace.backend.service;

import com.interviewace.backend.dto.InterviewProgressDto;
import com.interviewace.backend.dto.InterviewSummaryDto;

public interface InterviewAnalyticsService {
    InterviewSummaryDto getInterviewSummary(Long interviewId);
    InterviewProgressDto getMyProgress();
}
