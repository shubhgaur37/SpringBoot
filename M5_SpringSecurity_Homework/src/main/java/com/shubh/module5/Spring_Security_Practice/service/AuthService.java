package com.shubh.module5.Spring_Security_Practice.service;

import com.shubh.module5.Spring_Security_Practice.dto.LoginDTO;
import com.shubh.module5.Spring_Security_Practice.entity.User;
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
    JwtService jwtService;

    public String login(LoginDTO loginRequest) {
        // user should exist, handles JWT Creation and login flow through filter
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        User user = (User) authentication.getPrincipal();
        return jwtService.createJwtToken(user);
    }

}


