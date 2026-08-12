package com.interviewace.backend.repository;
import com.interviewace.backend.entity.InterviewAnswerEvaluation;
import com.interviewace.backend.entity.EvaluationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;
public interface InterviewAnswerEvaluationRepository extends JpaRepository<InterviewAnswerEvaluation, Long> {
    Optional<InterviewAnswerEvaluation> findByQuestionId(Long questionId);
    List<InterviewAnswerEvaluation> findByQuestionSessionIdAndStatus(Long sessionId, EvaluationStatus status);
    @Query("select e from InterviewAnswerEvaluation e join fetch e.question q join fetch q.session s where s.user.id = :userId and e.status = :status")
    List<InterviewAnswerEvaluation> findByUserIdAndStatus(Long userId, EvaluationStatus status);
    long countByQuestionSessionUserIdAndStatus(Long userId, EvaluationStatus status);
}
