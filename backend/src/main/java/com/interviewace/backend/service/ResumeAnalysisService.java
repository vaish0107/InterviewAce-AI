package com.interviewace.backend.service;
import com.interviewace.backend.dto.ResumeAnalysisDto;
public interface ResumeAnalysisService {
    ResumeAnalysisDto analyzeResume(Long resumeId);
    ResumeAnalysisDto getLatestAnalysis(Long resumeId);
}
