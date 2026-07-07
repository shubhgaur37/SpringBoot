package com.shubh.module5.Spring_Security_Demo.handlers;

import com.shubh.module5.Spring_Security_Demo.entity.UserEntity;
import com.shubh.module5.Spring_Security_Demo.repository.UserRepository;
import com.shubh.module5.Spring_Security_Demo.service.JWTService;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JWTService jwtService;
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
        // Otherwise, create a new local user record for future authentication/authorization.
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    return userRepository.save(newUser);
                });

        // Generate JWTs for our application.
        // Google authentication only verifies identity; from this point onward,
        // our application uses its own JWT-based authentication.
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        // Store the refresh token in an HttpOnly cookie so that JavaScript cannot
        // access it, reducing the risk of token theft through XSS attacks.
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
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
