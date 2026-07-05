package com.shubh.module5.Spring_Security_Practice.controller;


import com.shubh.module5.Spring_Security_Practice.dto.LoginDTO;
import com.shubh.module5.Spring_Security_Practice.dto.SignUpDTO;
import com.shubh.module5.Spring_Security_Practice.dto.UserResponseDTO;
import com.shubh.module5.Spring_Security_Practice.service.AuthService;
import com.shubh.module5.Spring_Security_Practice.service.UserService;
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

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    UserService userService;
    AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody SignUpDTO signUpRequest) {
        return ResponseEntity.ok(userService.signUp(signUpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginRequest, HttpServletRequest request, HttpServletResponse response) {
        String token = authService.login(loginRequest);
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(token);
    }


}
