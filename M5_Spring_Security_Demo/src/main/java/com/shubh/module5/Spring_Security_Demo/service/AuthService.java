package com.shubh.module5.Spring_Security_Demo.service;

import com.shubh.module5.Spring_Security_Demo.dto.LoginDTO;
import com.shubh.module5.Spring_Security_Demo.dto.LoginResponseDTO;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class AuthService {
    AuthenticationManager authenticationManager;
    SessionService sessionService;

    /*
     * =========================================================================
     * AuthService
     * =========================================================================
     *
     * Responsible for orchestrating the authentication workflow.
     *
     * Responsibilities:
     * - Authenticate user credentials.
     * - Delegate session creation to SessionService.
     * - Delegate Refresh Token validation and rotation to SessionService.
     * - Delegate logout and session invalidation to SessionService.
     *
     * Refactoring Note:
     * Session lifecycle management (token generation, Refresh Token
     * validation, Refresh Token Rotation, concurrent session handling and
     * logout) has been moved entirely into SessionService.
     *
     * This keeps AuthService focused solely on authentication while
     * SessionService owns the complete lifecycle of authenticated sessions.
     */

    // Login:
    // Spring Security's DaoAuthenticationProvider automatically invokes
    // PasswordEncoder.matches(rawPassword, encodedPassword) using the
    // configured PasswordEncoder bean.

    public LoginResponseDTO login(LoginDTO loginRequest) {

        // Authenticate the supplied user credentials.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // DaoAuthenticationProvider populates the authenticated principal
        // with our UserEntity (implements UserDetails).
        UserEntity user = (UserEntity) authentication.getPrincipal();
        // Delegate session creation and token generation.
        return sessionService.createSession(user);

    }

    public LoginResponseDTO refresh(String refreshToken) {
        // Delegate Refresh Token validation, Refresh Token Rotation and
        // token generation to SessionService.
        return sessionService.refreshSession(refreshToken);
    }

    public void logout(String refreshToken) {
        // Delegate logout to SessionService, which invalidates the
        // current authenticated session.
        sessionService.logout(refreshToken);
    }
}

/**
 * ============================================================================
 * ARCHITECTURAL FLOW: Authentication Mechanism
 * ============================================================================
 * <p>
 * 1. THE TRIGGER:
 * When authenticationManager.authenticate() is called with a token, the
 * ProviderManager (the default AuthenticationManager implementation) iterates
 * through its list of registered AuthenticationProviders.
 * <p>
 * 2. THE SELECTION:
 * The ProviderManager calls supports() on each provider. The
 * DaoAuthenticationProvider claims responsibility for
 * UsernamePasswordAuthenticationToken.
 * <p>
 * 3. THE BRIDGE (Provider -> Service):
 * - DaoAuthenticationProvider calls loadUserByUsername() on your
 * UserService (UserDetailsService implementation).
 * - This is where execution transitions from Spring Security into your
 * application code to load the user from the database.
 * - Once the UserEntity (implementing UserDetails) is returned,
 * DaoAuthenticationProvider verifies the supplied password using the
 * configured PasswordEncoder bean.
 * <p>
 * 4. THE RESULT:
 * If authentication succeeds, an authenticated Authentication object is
 * returned with the principal set to your fully populated UserEntity.
 * <p>
 * ============================================================================
 * AUTO-CONFIGURATION BEHAVIOR
 * ============================================================================
 * <p>
 * Spring Boot automatically wires this authentication flow when:
 * <p>
 * - A UserDetailsService bean is present.
 * - A PasswordEncoder bean is present.
 * <p>
 * AuthenticationConfiguration detects these beans, creates a
 * DaoAuthenticationProvider, injects your UserDetailsService and
 * PasswordEncoder into it, and registers it with the application's
 * AuthenticationManager.
 * <p>
 * No manual DaoAuthenticationProvider configuration is required when using
 * Spring Boot's default authentication setup.
 *
 */


