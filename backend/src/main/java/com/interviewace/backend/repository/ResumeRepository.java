package com.interviewace.backend.repository;
import com.interviewace.backend.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);
    Optional<Resume> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
