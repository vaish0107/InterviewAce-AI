package com.interviewace.backend.service.impl;

import com.interviewace.backend.exception.FileStorageException;
import com.interviewace.backend.exception.FileTooLargeException;
import com.interviewace.backend.exception.InvalidFileException;
import com.interviewace.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F', '-'};
    private final Path resumeDirectory;
    private final long maxResumeSize;

    public FileStorageServiceImpl(
            @Value("${app.upload.resume-directory}") String resumeDirectory,
            @Value("${app.upload.max-resume-size}") long maxResumeSize) {
        this.resumeDirectory = Path.of(resumeDirectory).toAbsolutePath().normalize();
        this.maxResumeSize = maxResumeSize;
    }

    @Override
    public String storeResume(MultipartFile file, Long userId) {
        validate(file);
        if (userId == null || userId < 1) throw new FileStorageException("A valid user is required to store a resume");

        Path userDirectory = resumeDirectory.resolve(userId.toString()).normalize();
        String storedFileName = UUID.randomUUID() + ".pdf";
        Path target = userDirectory.resolve(storedFileName).normalize();
        ensureInsideStorage(target);

        try {
            Files.createDirectories(userDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target.toString();
        } catch (IOException exception) {
            throw new FileStorageException("Unable to store resume file", exception);
        }
    }

    @Override
    public void deleteResumeFile(String storedFilePath) {
        if (storedFilePath == null || storedFilePath.isBlank()) {
            throw new FileStorageException("Stored resume path is invalid");
        }
        Path target = Path.of(storedFilePath).toAbsolutePath().normalize();
        ensureInsideStorage(target);
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new FileStorageException("Unable to delete resume file", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new InvalidFileException("Resume file must not be empty");
        if (file.getSize() > maxResumeSize) {
            throw new FileTooLargeException("Resume file exceeds the maximum allowed size of " + maxResumeSize + " bytes");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new InvalidFileException("Only PDF resume files are supported");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !"application/pdf".equalsIgnoreCase(contentType)) {
            throw new InvalidFileException("Resume MIME type must be application/pdf");
        }
        try (InputStream inputStream = file.getInputStream()) {
            byte[] signature = inputStream.readNBytes(PDF_SIGNATURE.length);
            if (signature.length != PDF_SIGNATURE.length) throw new InvalidFileException("File content is not a valid PDF");
            for (int index = 0; index < PDF_SIGNATURE.length; index++) {
                if (signature[index] != PDF_SIGNATURE[index]) throw new InvalidFileException("File content is not a valid PDF");
            }
        } catch (IOException exception) {
            throw new InvalidFileException("Unable to validate resume file");
        }
    }

    private void ensureInsideStorage(Path path) {
        if (path.equals(resumeDirectory) || !path.startsWith(resumeDirectory)) {
            throw new FileStorageException("Resume path is outside the configured storage directory");
        }
    }
}
