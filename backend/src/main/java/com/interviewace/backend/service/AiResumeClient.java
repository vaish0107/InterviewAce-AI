package com.interviewace.backend.service;
import com.interviewace.backend.dto.AiResumeAnalysisResponse;
import com.interviewace.backend.dto.AiJobMatchResponse;
import com.interviewace.backend.dto.AiQuestionGenerationRequest;
import com.interviewace.backend.dto.AiQuestionGenerationResponse;
import com.interviewace.backend.dto.AiAnswerEvaluationRequest;
import com.interviewace.backend.dto.AiAnswerEvaluationResponse;
import java.nio.file.Path;
public interface AiResumeClient {
    AiResumeAnalysisResponse analyzeResume(Path resumePath);
    String extractResumeText(Path resumePath);
    AiJobMatchResponse matchJob(Path resumePath, String jobDescription);
    AiQuestionGenerationResponse generateInterviewQuestions(AiQuestionGenerationRequest request);
    AiAnswerEvaluationResponse evaluateInterviewAnswer(AiAnswerEvaluationRequest request);
}
