package com.interviewace.backend.config;

import com.interviewace.backend.security.CustomUserDetailsService;
import com.interviewace.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter; private final CustomUserDetailsService userDetailsService;
    public SecurityConfig(JwtAuthenticationFilter jwtFilter, CustomUserDetailsService userDetailsService) { this.jwtFilter = jwtFilter; this.userDetailsService = userDetailsService; }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN)))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/register", "/api/auth/login", "/api/health",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll().anyRequest().authenticated())
                .authenticationProvider(authenticationProvider()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationProvider authenticationProvider() { DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService); provider.setPasswordEncoder(passwordEncoder()); return provider; }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception { return config.getAuthenticationManager(); }
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.frontend-url}") String frontendUrl) {
        CorsConfiguration config = new CorsConfiguration(); config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); config.setAllowedHeaders(List.of("Authorization", "Content-Type")); config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", config); return source;
    }
}
