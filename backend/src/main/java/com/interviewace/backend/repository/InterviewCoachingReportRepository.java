package com.interviewace.backend.repository;
import com.interviewace.backend.entity.InterviewCoachingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InterviewCoachingReportRepository extends JpaRepository<InterviewCoachingReport,Long>{
 Optional<InterviewCoachingReport> findTopBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
