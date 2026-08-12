package com.interviewace.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/health") @Tag(name = "Health")
public class HealthController { @GetMapping public Map<String, String> health() { return Map.of("status", "UP"); } }
