package com.interviewace.backend.controller;

import com.interviewace.backend.dto.DashboardSummaryDto;
import com.interviewace.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
@Tag(name = "Dashboard", description = "Authenticated user dashboard statistics from persisted data.")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping("/summary") @Operation(summary = "Get my dashboard summary")
    public DashboardSummaryDto summary() { return service.getMySummary(); }
}
