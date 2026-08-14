package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.exception.AiAnalysisException;
import com.interviewace.backend.exception.AiServiceUnavailableException;
import com.interviewace.backend.service.AiResumeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AiResumeClientImpl implements AiResumeClient {
    private final RestClient restClient;
    public AiResumeClientImpl(RestClient.Builder builder,
            @Value("${app.ai.base-url}") String baseUrl,
            @Value("${app.ai.connect-timeout-ms}") int connectTimeout,
            @Value("${app.ai.read-timeout-ms}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
    @Override
    public AiResumeAnalysisResponse analyzeResume(Path resumePath) {
        try {
            AiResumeAnalysisResponse response = restClient.post().uri("/api/resume/analyze-basic")
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(fileBody(requireFile(resumePath))).retrieve()
                    .body(AiResumeAnalysisResponse.class);
            if (response == null) throw new AiAnalysisException("AI service returned an invalid response");
            return response;
        } catch (RuntimeException exception) { throw translate(exception, "AI service could not analyze the resume"); }
    }
    @Override
    public String extractResumeText(Path resumePath) {
        try {
            AiResumeExtractionResponse response = restClient.post().uri("/api/resume/extract")
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(fileBody(requireFile(resumePath))).retrieve()
                    .body(AiResumeExtractionResponse.class);
            if (response == null || response.extractedText() == null || response.extractedText().isBlank())
                throw new AiAnalysisException("AI service returned invalid resume text");
            return response.extractedText();
        } catch (RuntimeException exception) { throw translate(exception, "AI service could not extract the resume"); }
    }
    @Override
    public AiJobMatchResponse matchJob(Path resumePath, String jobDescription) {
        String resumeText = extractResumeText(resumePath);
        try {
            AiJobMatchResponse response = restClient.post().uri("/api/resume/job-match")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AiJobMatchRequest(resumeText, jobDescription)).retrieve()
                    .body(AiJobMatchResponse.class);
            if (response == null) throw new AiAnalysisException("AI service returned an invalid job match response");
            return response;
        } catch (RuntimeException exception) { throw translate(exception, "AI service could not match the resume to the job"); }
    }
    @Override
    public AiQuestionGenerationResponse generateInterviewQuestions(AiQuestionGenerationRequest request) {
        try {
            AiQuestionGenerationResponse response = restClient.post().uri("/api/interview/questions")
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .body(AiQuestionGenerationResponse.class);
            if (response == null) throw new AiAnalysisException("AI service returned an invalid question response");
            return response;
        } catch (RuntimeException exception) { throw translate(exception, "AI service could not generate interview questions"); }
    }
    @Override
    public AiAnswerEvaluationResponse evaluateInterviewAnswer(AiAnswerEvaluationRequest request) {
        try {
            AiAnswerEvaluationResponse response = restClient.post().uri("/api/interview/evaluate-answer")
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve()
                    .body(AiAnswerEvaluationResponse.class);
            if (response == null) throw new AiAnalysisException("AI service returned an invalid answer evaluation");
            return response;
        } catch (ResourceAccessException exception) {
            throw new AiServiceUnavailableException("AI answer evaluation service is unavailable", exception);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 503 || exception.getStatusCode().value() == 504)
                throw new AiServiceUnavailableException("AI answer evaluation service is unavailable", exception);
            throw new AiAnalysisException("AI answer evaluation could not be completed", exception);
        } catch (AiAnalysisException | AiServiceUnavailableException exception) { throw exception; }
        catch (RuntimeException exception) { throw new AiAnalysisException("AI service returned an invalid answer evaluation", exception); }
    }
    @Override
    public AiFollowUpResponse generateFollowUp(AiFollowUpRequest request) {
        try {
            AiFollowUpResponse response = restClient.post().uri("/api/interview/follow-up")
                    .contentType(MediaType.APPLICATION_JSON).body(request).retrieve().body(AiFollowUpResponse.class);
            if (response == null || response.shouldAskFollowup() == null)
                throw new AiAnalysisException("AI service returned an invalid follow-up response");
            return response;
        } catch (ResourceAccessException exception) {
            throw new AiServiceUnavailableException("Adaptive follow-up service is unavailable", exception);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() >= 500)
                throw new AiServiceUnavailableException("Adaptive follow-up service is unavailable", exception);
            throw new AiAnalysisException("Adaptive follow-up could not be generated", exception);
        }
    }
    @Override public AiInterviewCoachingResponse generateInterviewCoaching(AiInterviewCoachingRequest request) {
        try {
            AiInterviewCoachingResponse response=restClient.post().uri("/api/interview/coaching").contentType(MediaType.APPLICATION_JSON)
                    .body(request).retrieve().body(AiInterviewCoachingResponse.class);
            if(response==null) throw new AiAnalysisException("AI service returned invalid coaching"); return response;
        } catch(ResourceAccessException ex){throw new AiServiceUnavailableException("AI coaching service is unavailable",ex);}
        catch(RestClientResponseException ex){if(ex.getStatusCode().is5xxServerError()) throw new AiServiceUnavailableException("AI coaching service is unavailable",ex); throw new AiAnalysisException("AI coaching could not be generated",ex);}
    }
    private Path requireFile(Path path) {
        Path normalized = path == null ? null : path.toAbsolutePath().normalize();
        if (normalized == null || !Files.isRegularFile(normalized)) throw new AiAnalysisException("Stored resume file is unavailable");
        return normalized;
    }
    private MultiValueMap<String, Object> fileBody(Path path) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(path)); return body;
    }
    private RuntimeException translate(RuntimeException exception, String responseMessage) {
        if (exception instanceof AiServiceUnavailableException || exception instanceof AiAnalysisException) return exception;
        if (exception instanceof ResourceAccessException)
            return new AiServiceUnavailableException("AI analysis service is unavailable", exception);
        if (exception instanceof RestClientResponseException)
            return new AiAnalysisException(responseMessage, exception);
        return new AiAnalysisException("AI service returned an invalid response", exception);
    }
}
