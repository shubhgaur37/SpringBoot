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
    JWTService jwtService;
    UserService userService;

    // Login:
    // Spring Security's DaoAuthenticationProvider automatically calls
    // PasswordEncoder.matches(rawPassword, storedHash) using the
    // PasswordEncoder bean configured in the application.
    public LoginResponseDTO login(LoginDTO loginRequest) {
        // principal is used in user details implementation to get the entity from DB
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // returns the email, principal passed in the request and maps it to email field of userEntity
        UserEntity user = (UserEntity) authentication.getPrincipal();

        // Create Access and Refresh Tokens
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        loginResponseDTO.setRefreshToken(refreshToken);
        return loginResponseDTO;
    }

    public LoginResponseDTO refresh(String refreshToken) {
        // returns error for malformed or expired jwt
        Long userId = jwtService.validateTokenGetUserId(refreshToken);
        UserEntity user = userService.findUserById(userId);

        String accessToken = jwtService.createAccessToken(user);
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        return loginResponseDTO;
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


