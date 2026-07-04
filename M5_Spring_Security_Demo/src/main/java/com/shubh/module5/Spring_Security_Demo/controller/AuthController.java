package com.shubh.module5.Spring_Security_Demo.controller;


import com.shubh.module5.Spring_Security_Demo.dto.LoginDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RestController
@RequestMapping("/auth")
public class AuthController {

    UserService userService;
    AuthService authService;

    @PostMapping("/signup")
    ResponseEntity<UserDTO> signUp(@RequestBody SignUpDTO signUpRequest) {
        return ResponseEntity.ok(userService.signUp(signUpRequest));
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginRequest,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse) {

        String token = authService.login(loginRequest);

        // Create a cookie to store the JWT securely
        Cookie cookie = new Cookie("token", token);

        /*
         * SECURITY NOTE: HttpOnly flag
         * 'HttpOnly(true)' prevents client-side JavaScript (e.g., document.cookie)
         * from accessing this cookie. This is a critical defense against
         * Cross-Site Scripting (XSS) attacks, ensuring that even if an attacker
         * injects a script, they cannot steal this authentication token.
         * Note: This has no impact on HTTP vs HTTPS; it restricts script access.
         */
        cookie.setHttpOnly(true);

        // Optional: cookie.setSecure(true); // Should be enabled in production to force HTTPS-only transmission

        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok(token);
    }

}
