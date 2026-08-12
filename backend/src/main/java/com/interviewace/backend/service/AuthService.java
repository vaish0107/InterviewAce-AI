package com.interviewace.backend.service;

import com.interviewace.backend.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileDto getCurrentUser();
}
