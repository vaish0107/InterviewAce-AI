package com.interviewace.backend.service;
import com.interviewace.backend.dto.AiResumeAnalysisResponse;
import com.interviewace.backend.dto.AiJobMatchResponse;
import com.interviewace.backend.dto.AiQuestionGenerationRequest;
import com.interviewace.backend.dto.AiQuestionGenerationResponse;
import com.interviewace.backend.dto.AiAnswerEvaluationRequest;
import com.interviewace.backend.dto.AiAnswerEvaluationResponse;
import com.interviewace.backend.dto.AiFollowUpRequest;
import com.interviewace.backend.dto.AiFollowUpResponse;
import com.interviewace.backend.dto.AiInterviewCoachingRequest;
import com.interviewace.backend.dto.AiInterviewCoachingResponse;
import com.interviewace.backend.dto.AiTargetedPracticeRequest;
import com.interviewace.backend.dto.AiTargetedPracticeResponse;
import java.nio.file.Path;
public interface AiResumeClient {
    AiResumeAnalysisResponse analyzeResume(Path resumePath);
    String extractResumeText(Path resumePath);
    AiJobMatchResponse matchJob(Path resumePath, String jobDescription);
    AiQuestionGenerationResponse generateInterviewQuestions(AiQuestionGenerationRequest request);
    AiAnswerEvaluationResponse evaluateInterviewAnswer(AiAnswerEvaluationRequest request);
    AiFollowUpResponse generateFollowUp(AiFollowUpRequest request);
    AiInterviewCoachingResponse generateInterviewCoaching(AiInterviewCoachingRequest request);
    AiTargetedPracticeResponse generateTargetedPracticeQuestions(AiTargetedPracticeRequest request);
}
