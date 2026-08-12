package com.interviewace.backend.security;

import com.interviewace.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expiration;
    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration = expiration;
    }
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder().subject(user.getEmail()).claim("role", user.getRole().name())
                .issuedAt(now).expiration(new Date(now.getTime() + expiration)).signWith(signingKey).compact();
    }
    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public String extractRole(String token) { return extractAllClaims(token).get("role", String.class); }
    public boolean isTokenValid(String token, UserDetails details) {
        try { return extractUsername(token).equalsIgnoreCase(details.getUsername()) && extractClaim(token, Claims::getExpiration).after(new Date()); }
        catch (RuntimeException exception) { return false; }
    }
    private <T> T extractClaim(String token, Function<Claims, T> resolver) { return resolver.apply(extractAllClaims(token)); }
    private Claims extractAllClaims(String token) { return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload(); }
}
