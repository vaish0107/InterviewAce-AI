package com.interviewace.backend.dto;

import com.interviewace.backend.entity.AnalysisStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ResumeAnalysisDto(Long id, Long resumeId, Integer atsScore, String grade,
        List<String> detectedSkills, List<String> strengths, List<String> weaknesses,
        List<String> recommendations, AnalysisStatus status, String scoringNote,
        LocalDateTime analyzedAt, String failureMessage) {}
