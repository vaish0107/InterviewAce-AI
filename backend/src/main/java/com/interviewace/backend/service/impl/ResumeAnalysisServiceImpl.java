package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class ResumeAnalysisServiceImpl implements ResumeAnalysisService {
    private final ResumeRepository resumes;
    private final ResumeAnalysisRepository analyses;
    private final AuthenticatedUserService authenticatedUsers;
    private final AiResumeClient aiClient;
    private final JsonListMapper lists;
    public ResumeAnalysisServiceImpl(ResumeRepository resumes, ResumeAnalysisRepository analyses,
            AuthenticatedUserService authenticatedUsers, AiResumeClient aiClient, JsonListMapper lists) {
        this.resumes = resumes; this.analyses = analyses; this.authenticatedUsers = authenticatedUsers;
        this.aiClient = aiClient; this.lists = lists;
    }
    @Override
    public ResumeAnalysisDto analyzeResume(Long resumeId) {
        Resume resume = ownedResume(resumeId);
        Path path = existingPath(resume.getFilePath());
        ResumeAnalysis analysis = analyses.saveAndFlush(new ResumeAnalysis(resume, AnalysisStatus.PROCESSING));
        resume.setUploadStatus(UploadStatus.PROCESSING);
        resumes.save(resume);
        try {
            AiResumeAnalysisResponse result = aiClient.analyzeResume(path);
            validate(result);
            analysis.setAtsScore(result.atsScore()); analysis.setGrade(result.grade());
            analysis.setDetectedSkills(lists.serialize(result.detectedSkills()));
            analysis.setStrengths(lists.serialize(result.strengths()));
            analysis.setWeaknesses(lists.serialize(result.weaknesses()));
            analysis.setSuggestions(lists.serialize(result.recommendations()));
            analysis.setScoringNote(result.scoringNote()); analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            analysis.setAnalyzedAt(LocalDateTime.now()); analysis.setFailureMessage(null);
            ResumeAnalysis saved = analyses.saveAndFlush(analysis);
            resume.setUploadStatus(UploadStatus.COMPLETED); resumes.save(resume);
            return toDto(saved);
        } catch (RuntimeException exception) {
            String safeMessage = safeFailureMessage(exception);
            analysis.setAnalysisStatus(AnalysisStatus.FAILED); analysis.setFailureMessage(safeMessage);
            analyses.saveAndFlush(analysis);
            resume.setUploadStatus(UploadStatus.FAILED); resumes.save(resume);
            if (exception instanceof AiServiceUnavailableException) throw exception;
            if (exception instanceof AiAnalysisException) throw exception;
            throw new AiAnalysisException(safeMessage, exception);
        }
    }
    @Override @Transactional(readOnly = true)
    public ResumeAnalysisDto getLatestAnalysis(Long resumeId) {
        Resume resume = ownedResume(resumeId);
        return analyses.findTopByResumeIdOrderByCreatedAtDesc(resume.getId()).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Resume analysis not found"));
    }
    private Resume ownedResume(Long id) {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        return resumes.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }
    private Path existingPath(String storedPath) {
        try {
            Path path = storedPath == null ? null : Path.of(storedPath).toAbsolutePath().normalize();
            if (path == null || !Files.isRegularFile(path)) throw new AiAnalysisException("Stored resume file is unavailable");
            return path;
        } catch (InvalidPathException exception) { throw new AiAnalysisException("Stored resume file is unavailable"); }
    }
    private void validate(AiResumeAnalysisResponse result) {
        if (result == null || result.atsScore() == null || result.atsScore() < 0 || result.atsScore() > 100)
            throw new AiAnalysisException("AI service returned an invalid ATS score");
        if (result.grade() == null || result.sectionScores() == null || result.detectedSkills() == null
                || result.strengths() == null || result.weaknesses() == null || result.recommendations() == null)
            throw new AiAnalysisException("AI service returned an invalid response");
    }
    private String safeFailureMessage(RuntimeException exception) {
        if (exception instanceof AiServiceUnavailableException) return "AI analysis service is unavailable";
        if (exception instanceof AiAnalysisException) return exception.getMessage();
        return "Resume analysis failed";
    }
    private ResumeAnalysisDto toDto(ResumeAnalysis value) {
        return new ResumeAnalysisDto(value.getId(), value.getResume().getId(), value.getAtsScore(), value.getGrade(),
                lists.deserialize(value.getDetectedSkills()), lists.deserialize(value.getStrengths()),
                lists.deserialize(value.getWeaknesses()), lists.deserialize(value.getSuggestions()),
                value.getAnalysisStatus(), value.getScoringNote(), value.getAnalyzedAt(), value.getFailureMessage());
    }
}
