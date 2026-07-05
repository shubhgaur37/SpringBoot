package com.shubh.module5.Spring_Security_Practice.filter;

import com.shubh.module5.Spring_Security_Practice.dto.UserResponseDTO;
import com.shubh.module5.Spring_Security_Practice.service.JwtService;
import com.shubh.module5.Spring_Security_Practice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Component
public class JWTAuthFilter extends OncePerRequestFilter {
    JwtService jwtService;
    UserService userService;
    ModelMapper modelMapper;
    HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring(7);
        try {
            Long userId = jwtService.validateToken(token);
            // null checks not required as they are being taken care of in auth service
            UserResponseDTO user = modelMapper.map(userService.getUserByID(userId), UserResponseDTO.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, null);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // authenticate the request and save authentication details to security context
            // holder for getting user context in service
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }
}
