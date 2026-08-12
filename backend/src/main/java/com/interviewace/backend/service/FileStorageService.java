package com.interviewace.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeResume(MultipartFile file, Long userId);
    void deleteResumeFile(String storedFilePath);
}
