package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.ResumeDto;
import com.interviewace.backend.dto.ResumeUploadResponse;
import com.interviewace.backend.entity.Resume;
import com.interviewace.backend.entity.UploadStatus;
import com.interviewace.backend.entity.User;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.ResumeRepository;
import com.interviewace.backend.service.AuthenticatedUserService;
import com.interviewace.backend.service.FileStorageService;
import com.interviewace.backend.service.ResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {
    private final ResumeRepository resumeRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final FileStorageService fileStorageService;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             AuthenticatedUserService authenticatedUserService,
                             FileStorageService fileStorageService) {
        this.resumeRepository = resumeRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ResumeUploadResponse uploadResume(MultipartFile file) {
        User user = authenticatedUserService.getAuthenticatedUser();
        String storedPath = fileStorageService.storeResume(file, user.getId());
        Resume resume = new Resume(user, safeOriginalFileName(file.getOriginalFilename()),
                Path.of(storedPath).getFileName().toString(), storedPath, "application/pdf", file.getSize());
        resume.setUploadStatus(UploadStatus.UPLOADED);
        try {
            Resume saved = resumeRepository.saveAndFlush(resume);
            return new ResumeUploadResponse(saved.getId(), saved.getOriginalFileName(), saved.getFileSize(),
                    saved.getUploadStatus(), "Resume uploaded successfully.", saved.getUploadedAt());
        } catch (RuntimeException exception) {
            try { fileStorageService.deleteResumeFile(storedPath); } catch (RuntimeException ignored) { exception.addSuppressed(ignored); }
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeDto> getMyResumes() {
        Long userId = authenticatedUserService.getAuthenticatedUser().getId();
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDto getMyResume(Long id) { return toDto(getOwnedResume(id)); }

    @Override
    @Transactional
    public void deleteMyResume(Long id) {
        Resume resume = getOwnedResume(id);
        fileStorageService.deleteResumeFile(resume.getFilePath());
        resumeRepository.delete(resume);
    }

    private Resume getOwnedResume(Long id) {
        Long userId = authenticatedUserService.getAuthenticatedUser().getId();
        return resumeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private ResumeDto toDto(Resume resume) {
        return new ResumeDto(resume.getId(), resume.getOriginalFileName(), resume.getFileType(),
                resume.getFileSize(), resume.getUploadStatus(), resume.getUploadedAt(), resume.getUpdatedAt());
    }

    private String safeOriginalFileName(String originalFileName) {
        String normalized = originalFileName == null ? "resume.pdf" : originalFileName.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isBlank()) name = "resume.pdf";
        return name.length() <= 255 ? name : name.substring(name.length() - 255);
    }
}
