package com.shubh.module5.Spring_Security_Practice.service;

import com.shubh.module5.Spring_Security_Practice.entity.Session;
import com.shubh.module5.Spring_Security_Practice.entity.User;
import com.shubh.module5.Spring_Security_Practice.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class JwtService {
    @Value("${jwt.secretKey}")
    String secretKey;

    final SessionRepository sessionRepository;

    public String createJwtToken(User user) {
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .signWith(getSecretKey())
                .compact();

        // 30 second validity of session
        Session session = new Session(user.getId(), token, LocalDateTime.now(), LocalDateTime.now().plusSeconds(30));
        sessionRepository.save(session);
        return token;
    }

    public Long validateToken(String token) {
        Claims claim = Jwts.parser().verifyWith(getSecretKey()).build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claim.getSubject());
        Session session = sessionRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("No Session for userId: " + userId + " exists"));

        if (!session.getToken().equals(token)) {
            throw new BadCredentialsException("Invalid session token.");
        }

        if (session.getExpiredAt().isBefore(LocalDateTime.now())) {
            // lazy deletion for expired token
            sessionRepository.deleteById(userId);
            throw new ExpiredJwtException(null, null, "Expired Token for userId: " + userId);
        }
        return userId;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
