package com.interviewace.backend.controller;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.service.ResumeService;
import com.interviewace.backend.service.ResumeAnalysisService;
import com.interviewace.backend.service.JobMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Resume Management", description = "Authenticated, owner-only PDF resume management and persisted ATS-style analysis.")
public class ResumeController {
    private final ResumeService resumeService;
    private final ResumeAnalysisService analysisService;
    private final JobMatchService jobMatchService;

    public ResumeController(ResumeService resumeService, ResumeAnalysisService analysisService,
            JobMatchService jobMatchService) {
        this.resumeService = resumeService; this.analysisService = analysisService; this.jobMatchService = jobMatchService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF resume", description = "Accepts a PDF-only multipart field named 'file' up to 5 MB. The resume belongs to the authenticated user.")
    public ResponseEntity<ResumeUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.uploadResume(file));
    }

    @GetMapping
    @Operation(summary = "List my resumes", description = "Returns only resumes owned by the authenticated user, including admins using this self-service API.")
    public List<ResumeDto> list() { return resumeService.getMyResumes(); }

    @GetMapping("/{id}")
    @Operation(summary = "Get my resume metadata", description = "The requested resume must belong to the authenticated user.")
    public ResumeDto get(@PathVariable Long id) { return resumeService.getMyResume(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete my resume", description = "Deletes an owned resume file and its metadata.")
    public void delete(@PathVariable Long id) { resumeService.deleteMyResume(id); }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Analyze my resume", description = "Calls the AI service synchronously and persists the latest ATS-style analysis.")
    public ResumeAnalysisDto analyze(@PathVariable Long id) { return analysisService.analyzeResume(id); }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Get my latest resume analysis", description = "Returns the most recently created persisted analysis for an owned resume.")
    public ResumeAnalysisDto analysis(@PathVariable Long id) { return analysisService.getLatestAnalysis(id); }

    @PostMapping("/{id}/job-match")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Match my resume to a job", description = "Extracts resume text in FastAPI, performs deterministic skill matching, and persists the result.")
    public JobMatchAnalysisDto jobMatch(@PathVariable Long id, @Valid @RequestBody JobMatchRequest request) {
        return jobMatchService.analyzeJobMatch(id, request);
    }

    @GetMapping("/{id}/job-matches")
    @Operation(summary = "List my resume's job matches", description = "Returns persisted job matches newest first for an owned resume.")
    public List<JobMatchAnalysisDto> jobMatches(@PathVariable Long id) { return jobMatchService.getJobMatchHistory(id); }

    @GetMapping("/{id}/job-match/latest")
    @Operation(summary = "Get my latest job match", description = "Returns the latest persisted job match for an owned resume.")
    public JobMatchAnalysisDto latestJobMatch(@PathVariable Long id) { return jobMatchService.getLatestJobMatch(id); }
}
