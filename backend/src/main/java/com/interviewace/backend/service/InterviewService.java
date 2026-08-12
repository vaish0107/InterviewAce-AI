package com.interviewace.backend.service;
import com.interviewace.backend.dto.*;
import java.util.List;
public interface InterviewService {
    InterviewSessionDto createInterview(CreateInterviewRequest request);
    InterviewSessionDto getInterview(Long id);
    List<InterviewSessionDto> getMyInterviews();
    InterviewSessionDto submitAnswer(Long sessionId, Long questionId, SubmitAnswerRequest request);
    InterviewSessionDto completeInterview(Long sessionId);
}
