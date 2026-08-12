package com.interviewace.backend.controller;

import com.interviewace.backend.dto.UserProfileDto;
import com.interviewace.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/users") @Tag(name = "Users")
public class UserController {
    private final AuthService authService;
    public UserController(AuthService authService) { this.authService = authService; }
    @GetMapping("/me") @PreAuthorize("hasAnyRole('USER', 'ADMIN')") public UserProfileDto me() { return authService.getCurrentUser(); }
}
