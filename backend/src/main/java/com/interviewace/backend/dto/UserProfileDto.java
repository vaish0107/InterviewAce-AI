package com.interviewace.backend.dto;

import com.interviewace.backend.entity.AccountStatus;
import com.interviewace.backend.entity.UserRole;
import java.time.LocalDateTime;

public record UserProfileDto(Long id, String fullName, String email, UserRole role, AccountStatus accountStatus,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {}
