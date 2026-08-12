package com.interviewace.backend.controller;

import com.interviewace.backend.dto.InterviewAnswerEvaluationDto;
import com.interviewace.backend.service.InterviewEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interviews/{sessionId}/questions/{questionId}")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Interview Answer Evaluation", description = "Owner-only AI-generated advisory feedback for saved answers.")
public class InterviewEvaluationController {
    private final InterviewEvaluationService service;
    public InterviewEvaluationController(InterviewEvaluationService service) { this.service = service; }
    @PostMapping("/evaluate") @Operation(summary = "Evaluate my saved answer with AI")
    public InterviewAnswerEvaluationDto evaluate(@PathVariable Long sessionId, @PathVariable Long questionId) {
        return service.evaluateAnswer(sessionId, questionId);
    }
    @GetMapping("/evaluation") @Operation(summary = "Get my persisted answer evaluation")
    public InterviewAnswerEvaluationDto get(@PathVariable Long sessionId, @PathVariable Long questionId) {
        return service.getEvaluation(sessionId, questionId);
    }
}
