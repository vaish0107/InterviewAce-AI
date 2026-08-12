package com.interviewace.backend.repository;
import com.interviewace.backend.entity.InterviewQuestionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface InterviewQuestionRecordRepository extends JpaRepository<InterviewQuestionRecord, Long> {
    List<InterviewQuestionRecord> findBySessionIdOrderByQuestionOrder(Long sessionId);
    Optional<InterviewQuestionRecord> findByIdAndSessionId(Long id, Long sessionId);
    List<InterviewQuestionRecord> findBySessionUserId(Long userId);
}
