package com.interviewace.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Validation failed", request, fields);
    }
    @ExceptionHandler(DuplicateResourceException.class) ResponseEntity<ApiErrorResponse> duplicate(DuplicateResourceException ex, HttpServletRequest req) { return error(HttpStatus.CONFLICT, ex.getMessage(), req, null); }
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiErrorResponse> missing(ResourceNotFoundException ex, HttpServletRequest req) { return error(HttpStatus.NOT_FOUND, ex.getMessage(), req, null); }
    @ExceptionHandler({InvalidFileException.class, InvalidResumeException.class, ResumeProcessingException.class}) ResponseEntity<ApiErrorResponse> invalidResume(RuntimeException ex, HttpServletRequest req) { return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null); }
    @ExceptionHandler({FileTooLargeException.class, MaxUploadSizeExceededException.class}) ResponseEntity<ApiErrorResponse> tooLarge(RuntimeException ex, HttpServletRequest req) { return error(HttpStatus.PAYLOAD_TOO_LARGE, "Resume file exceeds the maximum allowed size", req, null); }
    @ExceptionHandler({FileStorageException.class, StorageException.class}) ResponseEntity<ApiErrorResponse> storage(RuntimeException ex, HttpServletRequest req) { return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req, null); }
    @ExceptionHandler(AiServiceUnavailableException.class) ResponseEntity<ApiErrorResponse> aiUnavailable(AiServiceUnavailableException ex, HttpServletRequest req) { return error(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, null); }
    @ExceptionHandler(AiAnalysisException.class) ResponseEntity<ApiErrorResponse> aiAnalysis(AiAnalysisException ex, HttpServletRequest req) { return error(HttpStatus.BAD_GATEWAY, ex.getMessage(), req, null); }
    @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ApiErrorResponse> credentials(BadCredentialsException ex, HttpServletRequest req) { return error(HttpStatus.UNAUTHORIZED, "Invalid email or password", req, null); }
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException ex, HttpServletRequest req) { return error(HttpStatus.FORBIDDEN, "Access denied", req, null); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> generic(Exception ex, HttpServletRequest req) {
        log.error("Unhandled backend exception: {}", ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req, null);
    }
    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest req, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI(), fields));
    }
}
