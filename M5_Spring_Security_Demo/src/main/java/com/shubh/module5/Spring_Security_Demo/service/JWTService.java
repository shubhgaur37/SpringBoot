package com.shubh.module5.Spring_Security_Demo.service;

import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Service responsible for generating and validating JSON Web Tokens (JWT).
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class JWTService {

    // Injected from application.properties or application.yml
    // Ensure this key is sufficiently long (at least 32 bytes(256 bits) for HS256)
    @Value("${jwt.secretKey}")
    String secretKey;

    /**
     * Generates a signed JWT for a given user.
     *
     * @param user The user entity for whom the token is being created.
     * @return A compact, URL-safe JWT string.
     */
    public String createAccessToken(UserEntity user) {
        return Jwts.builder()
                // Set the 'sub' (subject) claim to identify the user
                .subject(user.getId().toString())
                // Add custom claims (payload information)
                .claim("Email", user.getEmail())
                .claim("Roles", List.of("ADMIN", "USER"))
                /*
                 * new Date() captures the current system timestamp.
                 * Note: Ensure all servers in a distributed system are synchronized via NTP
                 * to avoid issues with token expiration/validation due to clock drift.
                 * Consider using java.time.Instant for better precision in modern applications.
                 */
                .issuedAt(new Date())
                // Set the 'exp' (expiration) timestamp (currently set to 60 seconds)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 20))
                // Sign the token using the HMAC SHA algorithm
                .signWith(getSecretKey())
                // Build and serialize the token into a JWT string
                .compact();
    }

    /**
     * Generates a signed Refresh JWT for the given user.
     * <p>
     * Refresh tokens are long-lived and are used only to obtain new access tokens.
     * Unlike access tokens, they typically contain only the minimum required claims
     * (e.g., subject, issued time, expiration).
     */
    public String createRefreshToken(UserEntity user) {

        return Jwts.builder()
                // Set the 'sub' (subject) claim to identify the user
                .subject(user.getId().toString())
                .issuedAt(new Date())
                // Set the 'exp' (expiration) timestamp (currently set to 2 minutes)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2))
                // Sign the token using the HMAC SHA algorithm
                .signWith(getSecretKey())
                // Build and serialize the token into a JWT string
                .compact();
    }

    /**
     * Parses and validates a JWT string.
     *
     * @param token The JWT string received from the client.
     * @return The user ID extracted from the token subject.
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or tampered with.
     */
    public Long validateTokenGetUserId(String token) {
        // Parse the JWT, verify the signature, and extract the payload (Claims)
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        // Return the user ID stored in the subject
        return Long.valueOf(claims.getSubject());
    }

    /**
     * Converts the raw string secret into a secure SecretKey object.
     *
     * @return A SecretKey suitable for HMAC signing.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}