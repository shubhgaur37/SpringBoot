package com.shubh.module5.Spring_Security_Demo.service;

import com.shubh.module5.Spring_Security_Demo.dto.LoginResponseDTO;
import com.shubh.module5.Spring_Security_Demo.entity.Session;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.repository.SessionRepository;
import com.shubh.module5.Spring_Security_Demo.utils.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class SessionService {

    SessionRepository sessionRepository;
    JWTService jwtService;
    SubscriptionService subscriptionService;

    // Maximum number of concurrent sessions allowed per user.
    //private static final int MAX_SESSIONS = 2;

    /*
     * =========================================================================
     * SessionService
     * =========================================================================
     *
     * Responsible for managing the complete lifecycle of user sessions.
     *
     * Responsibilities:
     * - Create a new login session.
     * - Enforce the maximum concurrent session limit.
     * - Perform Refresh Token Rotation.
     * - Invalidate the current session during logout.
     *
     * Why separate this from AuthService?
     *
     * AuthService is responsible for orchestrating the authentication flow
     * (credential verification, token validation, etc.), whereas SessionService
     * owns everything related to the lifecycle of login sessions.
     *
     * This separation follows the Single Responsibility Principle (SRP),
     * making both services easier to understand, maintain and extend.
     *
     * =========================================================================
     * Refresh Token Rotation
     * =========================================================================
     *
     * Instead of reusing the same refresh token until it expires, every
     * successful refresh request generates:
     *
     *   1. A new Access Token.
     *   2. A new Refresh Token.
     *   3. A new Session.
     *
     * The previous refresh token and its corresponding session are immediately
     * invalidated.
     *
     * Example:
     *
     * User logs in
     * ---------------------
     * Refresh Token = R1
     *
     * An attacker somehow steals R1.
     *
     * Before the attacker can use it, the legitimate user refreshes first:
     *
     *      R1  ------------>  R2
     *
     * Server:
     *   - Creates Session(R2)
     *   - Deletes Session(R1)
     *
     * Later, the attacker attempts:
     *
     *      POST /auth/refresh (R1)
     *
     * Since Session(R1) no longer exists, the request is rejected and the
     * attacker cannot obtain another access token.
     *
     * Refresh Token Rotation therefore limits the usefulness of stolen
     * refresh tokens by making every refresh token single-use.
     */

    // Every successful login establishes a new authenticated session.
    public LoginResponseDTO createSession(UserEntity user) {

        // Retrieve all active sessions for the user.
        List<Session> userSessions = sessionRepository.findByUser(user);

        // Enforce the maximum concurrent session limit.
        // If the limit has been reached, remove the least recently used
        // session before creating a new one.
        if (userSessions.size() == subscriptionService.getSessionLimit(user.getPlan())) {

            userSessions.sort(Comparator.comparing(Session::getLastUsedAt));

            Session leastRecentlyUsedSession = userSessions.getFirst();

            sessionRepository.delete(leastRecentlyUsedSession);
        }

        // Generate a fresh Access Token and Refresh Token.
        LoginResponseDTO sessionTokens = createSessionTokens(user);

        // Persist the Refresh Token as a new authenticated session.
        Session newSession = Session.builder()
                .refreshToken(sessionTokens.getRefreshToken())
                .user(user)
                .build();

        sessionRepository.save(newSession);

        return sessionTokens;
    }


    // Refresh Token Rotation:
    //
    // Every successful refresh request creates a brand-new session with a
    // brand-new Refresh Token while immediately invalidating the previous
    // session. This ensures that every refresh token can be used only once.
    public LoginResponseDTO refreshSession(String refreshToken) {
        // Verify the Refresh Token's signature and expiration.
        // Throws if the token is malformed, tampered with or expired.
        jwtService.validateTokenGetUserId(refreshToken);

        // Verify that the supplied Refresh Token still has an active session.
        // This also prevents refreshing using tokens that were previously
        // invalidated due to logout, session eviction or token rotation.
        Session currentSession = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SessionAuthenticationException(
                        "No Session found for the refresh token: " + refreshToken));

        UserEntity user = currentSession.getUser();

        // Generate a new Access Token and Refresh Token.
        LoginResponseDTO sessionTokens = createSessionTokens(user);

        // Persist the newly generated Refresh Token as a new session.
        Session newSession = Session.builder()
                .refreshToken(sessionTokens.getRefreshToken())
                .user(user)
                .build();

        sessionRepository.save(newSession);

        // Invalidate the previous session so its Refresh Token can no
        // longer be used to obtain another Access Token.
        sessionRepository.delete(currentSession);

        return sessionTokens;
    }

    // Logout invalidates only the current device/browser session associated
    // with the supplied Refresh Token. Other active sessions for the same
    // user remain unaffected.
    public void logout(String refreshToken) {

        Session currentSession = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SessionAuthenticationException(
                        "No Session found for the refresh token: " + refreshToken));

        sessionRepository.delete(currentSession);
    }

    // Utility method used by both login and refresh flows to generate
    // a fresh pair of Access and Refresh Tokens.
    private LoginResponseDTO createSessionTokens(UserEntity user) {

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        LoginResponseDTO response = new LoginResponseDTO();

        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return response;
    }
}