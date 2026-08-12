package com.interviewace.backend.repository;
import com.interviewace.backend.entity.InterviewSession;
import com.interviewace.backend.entity.InterviewSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<InterviewSession> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, InterviewSessionStatus status);
}
