package com.interviewace.backend.controller;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Interview Practice", description = "Owner-only deterministic interview sessions. Answers are stored but not evaluated.")
public class InterviewController {
    private final InterviewService service;
    private final com.interviewace.backend.service.InterviewAnalyticsService analytics;
    public InterviewController(InterviewService service, com.interviewace.backend.service.InterviewAnalyticsService analytics) { this.service = service; this.analytics = analytics; }
    @PostMapping @Operation(summary = "Create and start an interview")
    public ResponseEntity<InterviewSessionDto> create(@Valid @RequestBody CreateInterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInterview(request));
    }
    @GetMapping @Operation(summary = "List my interview sessions")
    public List<InterviewSessionDto> list() { return service.getMyInterviews(); }
    @GetMapping("/{id}") @Operation(summary = "Get my interview session")
    public InterviewSessionDto get(@PathVariable Long id) { return service.getInterview(id); }
    @GetMapping("/{id}/summary") @Operation(summary = "Get deterministic analytics for my interview")
    public InterviewSummaryDto summary(@PathVariable Long id) { return analytics.getInterviewSummary(id); }
    @GetMapping("/progress") @Operation(summary = "Get my deterministic interview progress")
    public InterviewProgressDto progress() { return analytics.getMyProgress(); }
    @PutMapping("/{sessionId}/questions/{questionId}/answer") @Operation(summary = "Save an interview answer without scoring it")
    public InterviewSessionDto answer(@PathVariable Long sessionId, @PathVariable Long questionId,
            @Valid @RequestBody SubmitAnswerRequest request) { return service.submitAnswer(sessionId, questionId, request); }
    @PostMapping("/{sessionId}/questions/{questionId}/follow-up") @Operation(summary = "Generate an optional adaptive follow-up")
    public FollowUpGenerationDto followUp(@PathVariable Long sessionId, @PathVariable Long questionId) {
        return service.generateFollowUpQuestion(sessionId, questionId);
    }
    @PostMapping("/{id}/complete") @Operation(summary = "Complete my interview session")
    public InterviewSessionDto complete(@PathVariable Long id) { return service.completeInterview(id); }
}
