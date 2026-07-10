package com.shubh.module5.Spring_Security_Demo.controller;


import com.shubh.module5.Spring_Security_Demo.dto.LoginDTO;
import com.shubh.module5.Spring_Security_Demo.dto.LoginResponseDTO;
import com.shubh.module5.Spring_Security_Demo.dto.SignUpDTO;
import com.shubh.module5.Spring_Security_Demo.dto.UserDTO;
import com.shubh.module5.Spring_Security_Demo.service.AuthService;
import com.shubh.module5.Spring_Security_Demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RestController
@RequestMapping("/auth")
public class AuthController {

    final UserService userService;
    final AuthService authService;
    @Value("${deployment.env}")
    String deploymentType;

    @PostMapping("/signup")
    ResponseEntity<UserDTO> signUp(@RequestBody SignUpDTO signUpRequest) {
        return ResponseEntity.ok(userService.signUp(signUpRequest));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO loginRequest,
                                                  HttpServletRequest httpServletRequest,
                                                  HttpServletResponse httpServletResponse) {

        LoginResponseDTO loginResponse = authService.login(loginRequest);

        // Create a cookie to store the JWT securely

        //Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
        // Only the refresh token is stored in an HttpOnly cookie.
        // The short-lived access token is returned in the response body and is
        // expected to be sent by the client in the Authorization: Bearer header.
        // This reduces exposure of the refresh token to client-side JavaScript
        // while still allowing seamless token refresh.
        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        /*
         * SECURITY NOTE: HttpOnly flag
         * 'HttpOnly(true)' prevents client-side JavaScript (e.g., document.cookie)
         * from accessing this cookie. This is a critical defense against
         * Cross-Site Scripting (XSS) attacks, ensuring that even if an attacker
         * injects a script, they cannot steal this authentication token.
         * Note: This has no impact on HTTP vs HTTPS; it restricts script access.
         */
        //accessTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure("PRODUCTION".equals(deploymentType));

        // Optional: cookie.setSecure(true); // Should be enabled in production to force HTTPS-only transmission
        //httpServletResponse.addCookie(accessTokenCookie);
        httpServletResponse.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        // Edge Case
        if (cookies == null) {
            throw new AuthenticationServiceException("No cookies present");
        }

        // get refresh token from cookie
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside cookies"))
                .getValue();
        LoginResponseDTO refreshResponse = authService.refresh(refreshToken);
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure("PRODUCTION".equals(deploymentType));
        response.addCookie(refreshTokenCookie);

        // avoid sending refresh token as a response
        refreshResponse.setRefreshToken(null);

        return ResponseEntity.ok(refreshResponse);
    }


    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        // Edge case: the request does not contain any cookies.
        if (cookies == null) {
            throw new AuthenticationServiceException("No cookies present");
        }

        // Retrieve the refresh token stored in the HttpOnly cookie.
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside cookies"))
                .getValue();

        // Invalidate the server-side session associated with this refresh token.
        authService.logout(refreshToken);

        // Instruct the browser to immediately remove the refresh token cookie.
        // MaxAge(0) expires the cookie immediately.
        //
        // IMPORTANT:
        // The cookie path must match the path used when the cookie was created.
        // Since the login endpoint did not explicitly configure a path, the
        // browser stored the cookie with the default path "/auth". Therefore,
        // we use the same path here so the browser identifies and removes the
        // correct cookie.
        Cookie refreshTokenCookieInvalidated = new Cookie("refreshToken", "");
        // Not required here because the cookie was created under "/auth",
        // so the default cookie path is also "/auth".
        //refreshTokenCookieInvalidated.setPath("/auth");

        refreshTokenCookieInvalidated.setMaxAge(0);
        refreshTokenCookieInvalidated.setHttpOnly(true);
        refreshTokenCookieInvalidated.setSecure("PRODUCTION".equals(deploymentType));

        response.addCookie(refreshTokenCookieInvalidated);

        // 204 No Content indicates that logout completed successfully.
        return ResponseEntity.noContent().build();
    }

    /*
     * NOTE:
     * During testing, the refresh token cookie was manually modified by changing
     * its path from the original "/auth" to "/". Although the backend successfully
     * deleted the corresponding server-side session, the browser did not remove
     * the cookie because the invalidation cookie no longer matched the original
     * cookie attributes (name, path, domain).
     *
     * As a result, the client continued sending the stale refresh token cookie,
     * but the backend correctly rejected it since the server-side session had
     * already been invalidated.
     *
     * This demonstrates that the server-side session is the source of truth, not
     * the presence of a cookie. It also highlights the importance of a robust
     * frontend authentication flow. If a refresh request fails (401/403), the
     * frontend should clear its authentication state and redirect the user to
     * the login page instead of repeatedly attempting authenticated requests.
     */

}
