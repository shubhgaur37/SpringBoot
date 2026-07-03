package com.shubh.module5.Spring_Security_Demp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // tells spring boot that we want to configure security filter chain
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // Using default Spring Security configurations because no custom UserDetailsService Bean is explicitly defined.
        // This setup relies directly on the single-user credentials (shubh/shubh) specified in the application.yml file.
        return httpSecurity

                // Enables the standard, out-of-the-box Spring Security login UI page.
                // If this is completely omitted, the application will not render a form-based login interface.
                // For custom UI page rendering (e.g., templates/newlogin.html), swap this with:
                // .formLogin(formLoginConfig -> formLoginConfig.loginPage("/newlogin.html"))
                .formLogin(Customizer.withDefaults())

                // Evaluates and filters incoming HTTP requests before allowing access to controllers.
                // By default, this maintains a stateful session by passing JSESSIONID cookies back and forth.
                .authorizeHttpRequests(auth -> auth

                        // Public endpoint rule: Allows anyone to view the main posts feed without logging in.
                        .requestMatchers("/posts").permitAll()

                        // Multiple Roles Rule: Pass a comma-separated list of strings into .hasAnyRole().
                        // Any logged-in user carrying AT LEAST ONE of these roles (from your YAML list) can gain access.
                        // For example: Both an "ADMIN" and a "MANAGER" can manage sub-posts here.
                        .requestMatchers("/posts/**").hasAnyRole("ADMIN", "MANAGER")

                        // Catch-all safety rule: Mandates authentication for every other unmapped endpoint in the system.
                        .anyRequest().authenticated()
                )

                // Compiles and returns the finalized SecurityFilterChain instance.
                .build();
    }
    
}