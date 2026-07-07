package com.shubh.module5.Spring_Security_Demo.service;


import com.shubh.module5.Spring_Security_Demo.entity.Session;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.repository.SessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class SessionService {
    SessionRepository sessionRepository;

    // constant value
    private static final int MAX_SESSIONS = 2;

    // Every new login creates a new session
    public void createSession(UserEntity user, String refreshToken) {
        // find all user sessions
        List<Session> userSessions = sessionRepository.findByUser(user);

        // check if max_session limit is reached
        if (userSessions.size() == MAX_SESSIONS) {
            // get the least recently used session
            userSessions.sort(Comparator.comparing(Session::getLastUsedAt));
            Session leastRecentlyUsedSession = userSessions.getFirst();

            // delete the least recently used session
            sessionRepository.delete(leastRecentlyUsedSession);
        }

        // create a new session
        Session newSession = Session.builder().refreshToken(refreshToken).user(user).build();
        sessionRepository.save(newSession);
    }


    public void refreshSession(String refreshToken) {
        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SessionAuthenticationException("No Session found for the refresh token: " + refreshToken));
        // if a valid session is found update last used at for the session
        session.setLastUsedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}
