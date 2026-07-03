package com.shubh.module5.Spring_Security_Demp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity // Enables Spring Security and tells Spring Boot to look for a custom security filter chain
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // Configuring the SecurityFilterChain pipeline.
        // Since we defined a custom UserDetailsService Bean below,
        // Spring Boot overrides and ignores the single user credentials from application.yml.
        return httpSecurity

                // Enables standard form-based login authentication.
                // If this is omitted, the application will not render the default login UI.
                // For custom UI rendering (e.g., templates/newlogin.html), swap this with:
                // .formLogin(formLoginConfig -> formLoginConfig.loginPage("/newlogin.html"))
                .formLogin(Customizer.withDefaults()) // Configures form login with default settings

                // Intercepts and filters incoming HTTP requests based on roles and paths before reaching the controllers.
                .authorizeHttpRequests(auth -> auth

                        // Public Endpoint: Permits all unauthenticated users to access the main /posts route.
                        .requestMatchers("/posts").permitAll()

                        // Role-Based Authorization: Restricts access to matching sub-routes.
                        // User 'yash' can access this because he carries the 'MANAGER' role.
                        // User 'kalu' will receive an HTTP 403 Forbidden error because he only carries 'USER'.
                        .requestMatchers("/posts/**", "/error", "/public").hasAnyRole("ADMIN", "MANAGER")

                        // Catch-All Guard: Mandates authentication for every remaining unmapped endpoint.
                        .anyRequest().authenticated()
                )

                // Disables Cross-Site Request Forgery (CSRF) protection.
                // Safe to disable for stateless APIs using JWTs, allowing non-GET requests (POST, PUT, DELETE) from Postman without a token.
                .csrf(csrfConfig -> csrfConfig.disable())

                // Changes session tracking management from Stateful to Stateless.
                // CRITICAL: This is the exact reason your standard browser form login is now failing.
                // Stateless policy prevents Spring Security from saving user context in a HTTP Session or generating JSESSIONID cookies.
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Builds and compiles the finalized SecurityFilterChain bean instance
                .build();
    }


    // Configures an In-Memory User Store for testing user credentials
    @Bean
    UserDetailsService myInMemoryUserDetailsService() {

        // Passwords must be hashed using the PasswordEncoder bean to prevent plain-text storage vulnerabilities.
        // User 1 profile: Assigned a single authority role.
        UserDetails user1 = User.withUsername("kalu")
                .password(passwordEncoder().encode("hello"))
                .roles("USER")
                .build();

        // User 2 profile: Assigned multiple roles using Java's varargs (String...) feature.
        // This generates both ROLE_USER and ROLE_MANAGER authorities internally.
        UserDetails user2 = User.withUsername("yash")
                .password(passwordEncoder().encode("yash"))
                .roles("USER", "MANAGER")
                .build();

        // Returns the memory-mapped manager initialized with the list of users
        return new InMemoryUserDetailsManager(List.of(user1, user2));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Uses the BCrypt hashing algorithm to securely encode and verify user passwords
        return new BCryptPasswordEncoder();
    }
}
