package com.interviewace.backend.service;

import com.interviewace.backend.exception.FileStorageException;
import com.interviewace.backend.exception.FileTooLargeException;
import com.interviewace.backend.exception.InvalidFileException;
import com.interviewace.backend.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceImplTests {
    @TempDir Path tempDirectory;
    private FileStorageService service;

    @BeforeEach
    void setUp() { service = new FileStorageServiceImpl(tempDirectory.toString(), 10); }

    @Test void rejectsEmptyFiles() {
        assertThrows(InvalidFileException.class, () -> service.storeResume(pdf("cv.pdf", new byte[0]), 1L));
    }

    @Test void rejectsWrongExtensionAndMimeType() {
        assertThrows(InvalidFileException.class, () -> service.storeResume(
                new MockMultipartFile("file", "cv.txt", "text/plain", "%PDF-".getBytes()), 1L));
    }

    @Test void rejectsOversizedFiles() {
        assertThrows(FileTooLargeException.class, () -> service.storeResume(pdf("cv.pdf", "%PDF-123456".getBytes()), 1L));
    }

    @Test void rejectsNonPdfContent() {
        assertThrows(InvalidFileException.class, () -> service.storeResume(pdf("cv.pdf", "plain".getBytes()), 1L));
    }

    @Test void storesWithUuidNameInUserDirectoryAndDeletesIt() {
        String storedPath = service.storeResume(pdf("../cv.pdf", "%PDF-1".getBytes()), 7L);
        Path stored = Path.of(storedPath);
        assertTrue(Files.exists(stored));
        assertEquals(tempDirectory.resolve("7").toAbsolutePath().normalize(), stored.getParent());
        assertTrue(stored.getFileName().toString().matches("[0-9a-f-]{36}\\.pdf"));
        service.deleteResumeFile(storedPath);
        assertFalse(Files.exists(stored));
    }

    @Test void refusesDeletionOutsideStorage() {
        assertThrows(FileStorageException.class, () -> service.deleteResumeFile(tempDirectory.resolveSibling("outside.pdf").toString()));
    }

    private MockMultipartFile pdf(String name, byte[] bytes) {
        return new MockMultipartFile("file", name, "application/pdf", bytes);
    }
}
