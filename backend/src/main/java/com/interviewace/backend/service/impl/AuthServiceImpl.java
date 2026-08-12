package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.DuplicateResourceException;
import com.interviewace.backend.exception.ResourceNotFoundException;
import com.interviewace.backend.repository.UserRepository;
import com.interviewace.backend.security.JwtService;
import com.interviewace.backend.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service @Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager authenticationManager; private final JwtService jwtService;
    public AuthServiceImpl(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwtService) { this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.jwtService = jwtService; }
    @Override @Transactional public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email()); if (users.existsByEmailIgnoreCase(email)) throw new DuplicateResourceException("An account with this email already exists");
        User user = new User(request.fullName().trim(), email, encoder.encode(request.password())); user.setRole(UserRole.USER); user.setAccountStatus(AccountStatus.ACTIVE);
        return response(users.save(user));
    }
    @Override public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email()); authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return response(find(email));
    }
    @Override public UserProfileDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) throw new ResourceNotFoundException("Authenticated user not found");
        return profile(find(authentication.getName()));
    }
    private User find(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private AuthResponse response(User user) { return new AuthResponse(jwtService.generateToken(user), "Bearer", user.getId(), user.getFullName(), user.getEmail(), user.getRole()); }
    private UserProfileDto profile(User user) { return new UserProfileDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.getAccountStatus(), user.getCreatedAt(), user.getUpdatedAt()); }
}
