package com.shubh.module5.Spring_Security_Demo.service;

import com.shubh.module5.Spring_Security_Demo.dto.LoginDTO;
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
    // Login:
    // Spring Security's DaoAuthenticationProvider automatically calls
    // PasswordEncoder.matches(rawPassword, storedHash) using the
    // PasswordEncoder bean configured in the application.
    public String login(LoginDTO loginRequest) {
        // principal is used in user details implementation to get the entity from DB
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // returns the email, principal passed in the request and maps it to email field of userEntity
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return jwtService.createToken(user);
    }
}

/**
 * ARCHITECTURAL FLOW: Authentication Mechanism
 * -----------------------------------------------------------------------------
 * 1. THE TRIGGER:
 * When authenticationManager.authenticate() is called with a token, the
 * ProviderManager (the default AuthenticationManager implementation) iterates
 * through its list of registered AuthenticationProviders.
 * <p>
 * 2. THE SELECTION:
 * The ProviderManager calls .supports() on providers. The 'DaoAuthenticationProvider'
 * claims responsibility for UsernamePasswordAuthenticationToken.
 * <p>
 * 3. THE BRIDGE (Provider -> Service):
 * - The DaoAuthenticationProvider calls .loadUserByUsername() on your
 * UserService (our UserDetailsService implementation).
 * - This is how the execution flow jumps from framework code into your database logic.
 * - Once your UserEntity (implementing UserDetails) is returned, the provider
 * performs the password verification.
 * <p>
 * 4. THE RESULT:
 * If valid, the provider returns an authenticated Authentication object where
 * the 'principal' is set to your fully populated UserEntity.
 * <p>
 * -----------------------------------------------------------------------------
 * AUTO-CONFIGURATION BEHAVIOR:
 * Spring Boot's 'AuthenticationConfiguration' automatically wires this entire
 * process together if:
 * <p>
 * - A UserDetailsService (your UserService) bean is present.
 * - A PasswordEncoder bean is present.
 * <p>
 * Spring "magically" detects these and automatically instantiates a
 * 'DaoAuthenticationProvider', links your service to it, and registers the
 * whole stack into the primary AuthenticationManager. You do not need to
 * manually configure the DAO as long as these standard components exist.
 */