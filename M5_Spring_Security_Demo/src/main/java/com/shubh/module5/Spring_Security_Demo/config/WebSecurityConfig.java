package com.shubh.module5.Spring_Security_Demo.config;

import com.shubh.module5.Spring_Security_Demo.filter.JWTAuthFilter;
import com.shubh.module5.Spring_Security_Demo.handlers.Oauth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.shubh.module5.Spring_Security_Demo.entity.enums.Role.ADMIN;
import static com.shubh.module5.Spring_Security_Demo.entity.enums.Role.CREATOR;


@Configuration
@EnableWebSecurity // Enables Spring Security and tells Spring Boot to look for a custom security filter chain
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
            "/home.html"
    };

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
                // .formLogin(Customizer.withDefaults()) // Configures form login with default settings

                // Intercepts and filters incoming HTTP requests based on roles and paths before reaching the controllers.
                .authorizeHttpRequests(auth -> auth
                        // '/login' does not need to be explicitly permitted.
                        // When formLogin() or oauth2Login() is enabled, Spring Security internally
                        // registers and permits the login endpoint and related authentication URLs.
                        // These requests are handled by Spring Security's filters before reaching
                        // the application's authorization rules.

                        // Added static redirect home page for successful oauth login in endpoints without authorization
                        // Public Endpoint: Permits all unauthenticated users to access the main /posts route.
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        // -------------------------------------------------------------------------
                        // Endpoint-Based Authorization
                        // -------------------------------------------------------------------------
                        // Applies the same authorization rule to all HTTP methods for the matching path.
                        //
                        // "/posts/**" matches "/posts" as well as every nested path beneath it:
                        //
                        //   /posts              ✓
                        //   /posts/1            ✓
                        //   /posts/1/comments   ✓
                        //   /posts/a/b/c        ✓
                        //
                        // Since no HTTP method is specified, GET, POST, PUT, PATCH and DELETE
                        // requests to these paths all require the configured role(s).
                        //
                        // .requestMatchers("/posts/**").hasAnyRole(ADMIN.name())


                        // -------------------------------------------------------------------------
                        // Method + Endpoint-Based Authorization
                        // -------------------------------------------------------------------------
                        // Provides finer-grained access control by combining the HTTP method
                        // with the request path. Different operations on the same resource can
                        // therefore have different authorization requirements.
                        //
                        // Example:
                        //   GET    /posts/**  -> Public (Read)
                        //   POST   /posts     -> ADMIN or CREATOR (Create)
                        //   PUT    /posts/**  -> ADMIN only (Update)
                        //   DELETE /posts/**  -> ADMIN only (Delete)
                        // -------------------------------------------------------------------------
                        .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/posts").hasAnyRole(ADMIN.name(), CREATOR.name())

                        // Only Allow Creator and Admins to use the post route
                        // .requestMatchers("/posts").hasAnyRole(ADMIN.name(),CREATOR.name())

                        // Role-Based Authorization: Restricts access to matching sub-routes.
                        // User 'yash' can access this because he carries the 'MANAGER' role.
                        // User 'kalu' will receive an HTTP 403 Forbidden error because he only carries 'USER'.
                        // .requestMatchers("/posts/**").hasAnyRole("ADMIN", "MANAGER")

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
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // add custom filter before Username Password Authentication Filter
                .oauth2Login(oauth2Config ->
                        oauth2Config.failureUrl("/login?error=true") // Setting up oauth2 failure url for redirection
                                .successHandler(oauth2SuccessHandler)
                )
                // Builds and compiles the finalized SecurityFilterChain bean instance
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
