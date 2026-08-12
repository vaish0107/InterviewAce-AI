package com.interviewace.backend.service;

import com.interviewace.backend.dto.ResumeUploadResponse;
import com.interviewace.backend.entity.Resume;
import com.interviewace.backend.entity.UploadStatus;
import com.interviewace.backend.entity.User;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.ResumeRepository;
import com.interviewace.backend.service.impl.ResumeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeServiceImplTests {
    private ResumeRepository repository;
    private AuthenticatedUserService authenticatedUsers;
    private FileStorageService storage;
    private ResumeService service;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(ResumeRepository.class);
        authenticatedUsers = mock(AuthenticatedUserService.class);
        storage = mock(FileStorageService.class);
        user = new User("Jane", "jane@example.com", "hash");
        user.setId(1L);
        when(authenticatedUsers.getAuthenticatedUser()).thenReturn(user);
        service = new ResumeServiceImpl(repository, authenticatedUsers, storage);
    }

    @Test void uploadsMetadataWithoutAnalysisOrExtraction() {
        MockMultipartFile file = pdf();
        when(storage.storeResume(file, 1L)).thenReturn("uploads/resumes/1/stored.pdf");
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            Resume resume = invocation.getArgument(0);
            resume.setId(10L); resume.setUploadedAt(LocalDateTime.now()); resume.setUpdatedAt(LocalDateTime.now());
            return resume;
        });
        ResumeUploadResponse response = service.uploadResume(file);
        assertEquals(10L, response.id());
        assertEquals(UploadStatus.UPLOADED, response.uploadStatus());
        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
        verify(repository).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getExtractedText());
    }

    @Test void cleansStoredFileWhenDatabaseSaveFails() {
        MockMultipartFile file = pdf();
        String path = "uploads/resumes/1/stored.pdf";
        when(storage.storeResume(file, 1L)).thenReturn(path);
        when(repository.saveAndFlush(any())).thenThrow(new RuntimeException("database unavailable"));
        assertThrows(RuntimeException.class, () -> service.uploadResume(file));
        verify(storage).deleteResumeFile(path);
    }

    @Test void listsOnlyAuthenticatedUsersResumes() {
        when(repository.findByUserIdOrderByUploadedAtDesc(1L)).thenReturn(List.of());
        assertTrue(service.getMyResumes().isEmpty());
        verify(repository).findByUserIdOrderByUploadedAtDesc(1L);
    }

    @Test void hidesOtherUsersResumeAsNotFound() {
        when(repository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getMyResume(20L));
    }

    @Test void deletesOwnedFileAndMetadata() {
        Resume resume = resume();
        when(repository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(resume));
        service.deleteMyResume(10L);
        verify(storage).deleteResumeFile(resume.getFilePath());
        verify(repository).delete(resume);
    }

    private MockMultipartFile pdf() {
        return new MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1".getBytes());
    }

    private Resume resume() {
        Resume resume = new Resume(user, "resume.pdf", "stored.pdf", "uploads/resumes/1/stored.pdf", "application/pdf", 6L);
        resume.setId(10L); resume.setUploadedAt(LocalDateTime.now()); resume.setUpdatedAt(LocalDateTime.now());
        return resume;
    }
}
