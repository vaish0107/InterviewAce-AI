package com.interviewace.backend.service.impl;

import com.interviewace.backend.entity.User;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.UserRepository;
import com.interviewace.backend.service.AuthenticatedUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {
    private final UserRepository userRepository;

    public AuthenticatedUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
