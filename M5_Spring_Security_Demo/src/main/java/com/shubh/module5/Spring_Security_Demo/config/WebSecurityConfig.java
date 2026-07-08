package com.shubh.module5.Spring_Security_Demo.config;


import com.shubh.module5.Spring_Security_Demo.filter.JWTAuthFilter;
import com.shubh.module5.Spring_Security_Demo.handlers.Oauth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity // Enables Spring Security and tells Spring Boot to look for a custom security filter chain
@EnableMethodSecurity(securedEnabled = true) // secureEnabled should be true to use @Secured annotation
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JWTAuthFilter jwtAuthFilter;
    private final Oauth2SuccessHandler oauth2SuccessHandler;

    // Endpoints that do not require authentication.
    // "/error" is Spring Boot's default error endpoint used as a fallback when
    // requests cannot be handled or an unhandled exception occurs.
    private static final String[] PUBLIC_ROUTES = {
            "/error",
            "/auth/**",
            "/home.html"  // Added static redirect home page for successful oauth login in endpoints without authorization
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // Configures the Spring Security filter chain.
        //
        // Best Practice:
        // Keep the filter chain responsible only for deciding whether an endpoint
        // is public or requires authentication. Delegate business-specific
        // authorization (roles, permissions, ownership checks, etc.) to
        // @Secured/@PreAuthorize on controller or service methods.
        //
        // Advantages:
        // 1. Clear separation of concerns (Authentication vs Authorization).
        // 2. Keeps the filter chain concise and easier to understand.
        // 3. Avoids duplicating authorization rules in multiple places.
        // 4. Enables fine-grained authorization using SpEL expressions,
        //    custom bean methods, ownership checks, etc.
        // 5. Security rules remain close to the business methods they protect,
        //    making them easier to maintain and less error-prone.
        return httpSecurity

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints accessible without authentication.
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // All post endpoints require an authenticated user.
                        // Business-specific authorization is performed later using
                        // method security annotations (@Secured/@PreAuthorize),
                        // keeping the filter chain simple and avoiding duplicated
                        // authorization rules.
                        .requestMatchers("/posts/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Disables CSRF protection.
                // Safe for stateless JWT-based APIs since requests are not
                // authenticated using server-side HTTP sessions.
                .csrf(csrfConfig -> csrfConfig.disable())

                // Configure Spring Security to operate in stateless mode.
                // Every request must authenticate itself (e.g. using a JWT)
                // because no HttpSession is created or reused.
                .sessionManagement(sessionConfig ->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Execute JWT authentication before Spring's built-in
                // UsernamePasswordAuthenticationFilter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .oauth2Login(oauth2Config ->
                        oauth2Config
                                .failureUrl("/login?error=true")
                                .successHandler(oauth2SuccessHandler)
                )

                .build();
    }

    /**
     * Configuration class for Spring Security.
     * <p>
     * ARCHITECTURAL NOTE: The link between AuthenticationManager and UserDetailsService
     * -------------------------------------------------------------------------------
     * The AuthenticationManager is the central interface for authentication, acting as
     * the "Engine." However, it is inherently "blind"—it does not know how or where
     * your users are stored.
     * <p>
     * The connection is established via the 'DaoAuthenticationProvider':
     * <p>
     * 1. DELEGATION: The AuthenticationManager delegates validation to one or more
     * AuthenticationProviders.
     * <p>
     * 2. THE BRIDGE (DaoAuthenticationProvider): This provider is specifically
     * designed to use a 'UserDetailsService' (our UserService) as its data source.
     * <p>
     * 3. THE WORKFLOW:
     * - When a login request occurs, the AuthenticationManager calls the provider.
     * - The provider calls 'loadUserByUsername()' on our UserDetailsService.
     * - Our UserDetailsService retrieves the user record from the database.
     * - The provider then compares the provided password with the password
     * loaded from the database using a PasswordEncoder.
     * <p>
     * NOTE: If your service class also tries to inject the AuthenticationManager
     * (e.g., to handle manual logins), you will create a Circular Dependency.
     * This is why we separate Authentication logic into a dedicated AuthService
     * or use @Lazy injection.
     */

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }


    // Configures an In-Memory User Store for testing user credentials
//    @Bean
//    UserDetailsService myInMemoryUserDetailsService() {
//
//        // Passwords must be hashed using the PasswordEncoder bean to prevent plain-text storage vulnerabilities.
//        // User 1 profile: Assigned a single authority role.
//        UserDetails user1 = User.withUsername("kalu")
//                .password(passwordEncoder().encode("hello"))
//                .roles("USER")
//                .build();
//
//        // User 2 profile: Assigned multiple roles using Java's varargs (String...) feature.
//        // This generates both ROLE_USER and ROLE_MANAGER authorities internally.
//        UserDetails user2 = User.withUsername("yash")
//                .password(passwordEncoder().encode("yash"))
//                .roles("USER", "MANAGER")
//                .build();
//
//        // Returns the memory-mapped manager initialized with the list of users
//        return new InMemoryUserDetailsManager(List.of(user1, user2));
//    }


    /*
    * Password Encoder moved from here as it created a circular dependency
    -----─────┐
    |  JWTAuthFilter defined in file [/Users/shubhgaur/Documents/SpringBOOT/M5_Spring_Security_Demo/target/classes/com/shubh/module5/Spring_Security_Demo/filter/JWTAuthFilter.class]
    ↑     ↓
    |  userService defined in file [/Users/shubhgaur/Documents/SpringBOOT/M5_Spring_Security_Demo/target/classes/com/shubh/module5/Spring_Security_Demo/service/UserService.class]
    ↑     ↓
    |  webSecurityConfig defined in file [/Users/shubhgaur/Documents/SpringBOOT/M5_Spring_Security_Demo/target/classes/com/shubh/module5/Spring_Security_Demo/config/WebSecurityConfig.class]
    └─────┘

    * */
}
