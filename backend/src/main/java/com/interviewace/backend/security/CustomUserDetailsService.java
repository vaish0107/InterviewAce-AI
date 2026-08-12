package com.interviewace.backend.security;

import com.interviewace.backend.entity.AccountStatus;
import com.interviewace.backend.entity.User;
import com.interviewace.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }
    @Override public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash()).roles(user.getRole().name())
                .disabled(user.getAccountStatus() == AccountStatus.INACTIVE)
                .accountLocked(user.getAccountStatus() == AccountStatus.LOCKED).build();
    }
}
