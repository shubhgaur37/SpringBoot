package com.shubh.module5.Spring_Security_Demo.handlers;

import com.shubh.module5.Spring_Security_Demo.dto.LoginResponseDTO;
import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.entity.enums.Role;
import com.shubh.module5.Spring_Security_Demo.entity.enums.SubscriptionPlan;
import com.shubh.module5.Spring_Security_Demo.repository.UserRepository;
import com.shubh.module5.Spring_Security_Demo.service.SessionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    @Value("${deployment.env}")
    String deploymentType;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Spring Security populates the Authentication object after Google successfully
        // authenticates the user. Cast it to OAuth2AuthenticationToken to access
        // provider-specific information.
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        // Retrieve the authenticated Google user's profile information.
        OAuth2User oAuth2User = token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // If the user has already signed in before, reuse the existing account.
        // Otherwise, create a new local user record for future authentication
        // and authorization within our application.
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setName(name);

                    // OAuth registration does not receive a signup payload.
                    // Assign the default subscription as part of the user
                    // registration flow rather than in the entity itself.
                    newUser.setPlan(SubscriptionPlan.FREE);
                    newUser.setRoles(Set.of(Role.USER));
                    return userRepository.save(newUser);
                });

        // Create an authenticated application session.
        //
        // Previously, the OAuth flow generated JWTs directly using JWTService,
        // which meant no server-side session was created. As a result:
        //
        // - The refresh token was never persisted in the sessions table.
        // - Concurrent session limits were not enforced.
        // - Refresh Token Rotation was not supported.
        // - The /auth/refresh endpoint failed because it expected an active
        //   session corresponding to the supplied refresh token.
        //
        // By delegating to SessionService, the OAuth login now follows the same
        // authentication flow as username/password login, ensuring consistent
        // session management across all authentication mechanisms.
        LoginResponseDTO sessionTokens = sessionService.createSession(user);
        String accessToken = sessionTokens.getAccessToken();
        String refreshToken = sessionTokens.getRefreshToken();

        // Store the refresh token in an HttpOnly cookie so that JavaScript cannot
        // access it, reducing the risk of token theft through XSS attacks.
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);

        // Scope the cookie to the application's root path ("/").
        //
        // The browser always stores the cookie when it receives the Set-Cookie
        // header. The Path attribute controls which subsequent requests will
        // include the cookie.
        //
        // Since this cookie is created during the OAuth callback
        // (/login/oauth2/code/google), omitting setPath("/") would scope it to
        // the callback path by default. As a result, the browser would not send
        // it to other endpoints such as /auth/refresh or /auth/logout.
        //
        // Setting the path to "/" makes the cookie available to the entire
        // application on the same host (e.g. localhost).
        refreshTokenCookie.setPath("/");

        refreshTokenCookie.setHttpOnly(true);

        // Only transmit the cookie over HTTPS in production.
        refreshTokenCookie.setSecure("PRODUCTION".equals(deploymentType));

        // Adds a Set-Cookie header to the HTTP response.
        // The browser stores this cookie before following the redirect.
        response.addCookie(refreshTokenCookie);

        // Redirect the user to the application's success page, passing the access
        // token as a query parameter for the frontend to consume.
        String frontEndUrl = "http://localhost:8080/home.html?token=" + accessToken;

        // Sends an HTTP 302 redirect (Location header). The browser processes both
        // the Set-Cookie and Location headers together—saving the refresh token
        // cookie first, then navigating to the success page.
        response.sendRedirect(frontEndUrl);

        // Equivalent Spring Security helper:
        // getRedirectStrategy().sendRedirect(request, response, frontEndUrl);
    }
}
