package com.interviewace.backend.service;

import com.interviewace.backend.entity.User;

public interface AuthenticatedUserService {
    User getAuthenticatedUser();
}
