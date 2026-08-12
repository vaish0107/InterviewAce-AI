package com.interviewace.backend.controller;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request)); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }
}
