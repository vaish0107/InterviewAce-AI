package com.interviewace.backend.service;
import com.interviewace.backend.dto.ResumeDto;
import com.interviewace.backend.dto.ResumeUploadResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
public interface ResumeService {
    ResumeUploadResponse uploadResume(MultipartFile file);
    List<ResumeDto> getMyResumes();
    ResumeDto getMyResume(Long id);
    void deleteMyResume(Long id);
}
