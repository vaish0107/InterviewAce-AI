package com.interviewace.backend.repository;

import com.interviewace.backend.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.interviewace.backend.entity.AnalysisStatus;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    Optional<ResumeAnalysis> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);
    List<ResumeAnalysis> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    long countByResumeUserIdAndAnalysisStatus(Long userId, AnalysisStatus status);
    Optional<ResumeAnalysis> findTopByResumeUserIdAndAnalysisStatusOrderByAnalyzedAtDesc(Long userId, AnalysisStatus status);
}
