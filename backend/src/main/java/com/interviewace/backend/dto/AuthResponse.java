package com.interviewace.backend.dto;

import com.interviewace.backend.entity.UserRole;

public record AuthResponse(String token, String tokenType, Long userId, String fullName, String email, UserRole role) {}
