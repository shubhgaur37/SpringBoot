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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        /*
         * SECURITY NOTE: HttpOnly flag
         * 'HttpOnly(true)' prevents client-side JavaScript (e.g., document.cookie)
         * from accessing this cookie. This is a critical defense against
         * Cross-Site Scripting (XSS) attacks, ensuring that even if an attacker
         * injects a script, they cannot steal this authentication token.
         * Note: This has no impact on HTTP vs HTTPS; it restricts script access.
         */
        accessTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setHttpOnly(true);

        // Optional: cookie.setSecure(true); // Should be enabled in production to force HTTPS-only transmission
        httpServletResponse.addCookie(accessTokenCookie);
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

        LoginResponseDTO loginResponse = authService.refresh(refreshToken);

        // Create a cookie to store the JWT securely
        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
        /*
         * SECURITY NOTE: HttpOnly flag
         * 'HttpOnly(true)' prevents client-side JavaScript (e.g., document.cookie)
         * from accessing this cookie. This is a critical defense against
         * Cross-Site Scripting (XSS) attacks, ensuring that even if an attacker
         * injects a script, they cannot steal this authentication token.
         * Note: This has no impact on HTTP vs HTTPS; it restricts script access.
         */
        accessTokenCookie.setHttpOnly(true);

        // Should be enabled in production to force HTTPS-only transmission, controlled using env variable in yaml
        accessTokenCookie.setSecure("Production".equals(deploymentType));

        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(loginResponse);
    }

}
