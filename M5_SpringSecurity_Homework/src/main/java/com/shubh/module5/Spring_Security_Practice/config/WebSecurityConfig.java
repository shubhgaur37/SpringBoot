package com.shubh.module5.Spring_Security_Practice.config;

import com.shubh.module5.Spring_Security_Practice.filter.JWTAuthFilter;
import com.shubh.module5.Spring_Security_Practice.filter.LoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final LoggingFilter loggingFilter;
    private final JWTAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http

                // Disable CSRF since this is a stateless REST API (e.g., JWT authentication).
                .csrf(csrf -> csrf.disable())

                // Do not create or use HTTP sessions.
                // Every request should carry its own authentication information (JWT).
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /*
                 * Configure authorization rules.
                 *
                 * Spring Security evaluates these rules IN THE ORDER they are declared.
                 *
                 * It stops at the FIRST matching rule.
                 *
                 * Example:
                 *
                 * requestMatchers("/auth/**").permitAll()
                 * requestMatchers("/admin/**").hasRole("ADMIN")
                 * anyRequest().authenticated()
                 *
                 * Request: /auth/login
                 *   -> Matches first rule
                 *   -> permitAll()
                 *   -> Remaining rules are NOT checked.
                 *
                 * Request: /admin/users
                 *   -> Doesn't match "/auth/**"
                 *   -> Matches "/admin/**"
                 *   -> Requires ADMIN role
                 *
                 * Request: /employee/profile
                 *   -> Doesn't match first two rules
                 *   -> Falls back to anyRequest()
                 */
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/posts", "/error", "/auth/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()

                        /*
                         * Catch-all rule.
                         *
                         * anyRequest() matches EVERYTHING that has not matched
                         * one of the requestMatchers above.
                         *
                         * Since it matches every remaining request, it MUST
                         * always be the LAST authorization rule.
                         */
                        //.anyRequest().permitAll()

                        /*
                         * Using permitAll() means:
                         *
                         * - Every request is allowed.
                         * - No authentication is required.
                         * - No roles/authorities are checked.
                         *
                         * IMPORTANT:
                         * The Security Filter Chain STILL executes.
                         *
                         * Filters such as:
                         *   - LoggingFilter
                         *   - JWTAuthFilter
                         *   - CORS Filter
                         * etc.
                         * will still run.
                         *
                         * permitAll() only skips the authorization requirement.
                         */
                )
                // Register filters in an explicit order.
                // If multiple filters are added relative to the same anchor filter,
                // Spring Security guarantees they execute before/after the anchor,
                // but their relative order is not guaranteed.
                .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
/*                // Test filter that throws an exception before the request reaches Spring MVC.
                //
                // Observations:
                //
                // 1. Request Payload:
                //    Empty because no downstream component (e.g., Jackson) ever reads the
                //    request InputStream, so ContentCachingRequestWrapper never caches it.
                //
                // 2. Response Payload:
                //    Empty because the exception escapes the filter chain before any response
                //    body is written.
                //
                // 3. Response Status:
                //    Logged as 200 because the LoggingFilter's finally block executes before
                //    Spring Boot converts the exception into the final 500 error response.
                .addFilterAfter(new OncePerRequestFilter() {

                    @Override
                    protected void doFilterInternal(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    FilterChain filterChain)
                            throws ServletException, IOException {

                        throw new RuntimeException("Exception before controller");
                    }

                }, LoggingFilter.class)*/
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }

}
