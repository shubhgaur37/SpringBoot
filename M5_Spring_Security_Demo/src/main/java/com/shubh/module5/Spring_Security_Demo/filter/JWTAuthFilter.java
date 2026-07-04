package com.shubh.module5.Spring_Security_Demo.filter;

import com.shubh.module5.Spring_Security_Demo.dto.UserDTO;
import com.shubh.module5.Spring_Security_Demo.service.JWTService;
import com.shubh.module5.Spring_Security_Demo.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWTAuthFilter: Handles stateless authentication for the application.
 * This filter intercepts every incoming request to validate the JWT token
 * provided in the 'Authorization' header.
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Component
public class JWTAuthFilter extends OncePerRequestFilter {

    JWTService jwtService;
    UserService userService;
    ModelMapper modelMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * 1. EXTRACTION:
         * Retrieve the 'Authorization' header. Since this filter runs globally
         * for every request, we must first determine if a token is present.
         */
        String authHeader = request.getHeader("Authorization");

        /*
         * 2. SHORT-CIRCUIT:
         * If the header is missing or does not start with "Bearer ", we bypass
         * JWT logic and hand off to the next filter in the chain. This ensures
         * that public routes (e.g., /auth/**) proceed without interruption.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * 3. VALIDATION:
         * Extract the JWT token (skipping the "Bearer " prefix) and validate
         * it using the JWTService.
         */
        String token = authHeader.substring(7);
        Long userId = jwtService.validateToken(token);

        /*
         * 4. SECURITY CONTEXT POPULATION:
         * If the token is valid and no authentication exists in the current
         * SecurityContext, we load the user from the database and set the
         * authentication object. This allows downstream controllers to
         * access the user via SecurityContextHolder.
         */
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDTO user = modelMapper.map(userService.findUserById(userId), UserDTO.class);

            // Populate token with user details and authorities
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, null);

            /*
             * DETAIL BINDING:
             * We bind request metadata (such as the remote IP address) to the
             * authentication object. This is highly useful for downstream
             * security logic, such as:
             * - Rate-limiting requests based on IP address.
             * - Security auditing and logging of access patterns.
             * Note: While WebAuthenticationDetails also captures Session IDs,
             * we are in a stateless flow, so that field will be null.
             */
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Finalize: Set the context so Spring Security treats this request as authenticated
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        /*
         * 5. CHAIN CONTINUATION:
         * IMPORTANT: Always call filterChain.doFilter() to pass the request
         * to the next component in the security pipeline. Do not call this
         * filter again (no recursion), or it will cause a StackOverflowError.
         */
        filterChain.doFilter(request, response);
    }
}


/* FILTER CHAIN BEHAVIOR (JWTAuthFilter):
 *
 * - GLOBAL SCOPE: Filters are not routes. Even if a path is defined as
 *   'permitAll()' in your SecurityFilterChain, your custom 'JWTAuthFilter'
 *   runs globally for EVERY request because it sits in the Security Filter Chain
 *   before authorization logic.
 *
 * - THE PIPELINE: Think of the filter chain as an airport security gate. Your
 *   filter is a checkpoint that every request must pass through first. If it
 *   is a public route, your filter should simply call 'filterChain.doFilter()'
 *   to pass the request along to the next step in the pipeline.
 *
 * - RECURSION WARNING: Never call 'this.doFilterInternal()' from within your
 *   filter. Always call 'filterChain.doFilter(request, response)' to pass the
 *   request to the next filter. Calling your own method will cause an
 *   infinite loop, resulting in a StackOverflowError.
 */