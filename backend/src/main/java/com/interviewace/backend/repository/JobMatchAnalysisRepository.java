package com.interviewace.backend.repository;
import com.interviewace.backend.entity.JobMatchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface JobMatchAnalysisRepository extends JpaRepository<JobMatchAnalysis, Long> {
    List<JobMatchAnalysis> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    Optional<JobMatchAnalysis> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);
    long countByResumeUserId(Long userId);
    Optional<JobMatchAnalysis> findTopByResumeUserIdOrderByCreatedAtDesc(Long userId);
}
