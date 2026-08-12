package com.interviewace.backend.service;
import com.interviewace.backend.dto.InterviewAnswerEvaluationDto;
public interface InterviewEvaluationService {
    InterviewAnswerEvaluationDto evaluateAnswer(Long sessionId, Long questionId);
    InterviewAnswerEvaluationDto getEvaluation(Long sessionId, Long questionId);
}
