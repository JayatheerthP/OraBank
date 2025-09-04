package com.jayatheerth.userservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jayatheerth.userservice.exception.UserServiceException;

/**
 * Configuration class for setting up security in the application. This class
 * enables web security and method-level security, configures HTTP security
 * settings, and integrates a custom JWT authentication filter for stateless
 * authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructs the SecurityConfig with a JWTAuthenticationFilter dependency.
     *
     * @param jwtAuthenticationFilter The custom filter for JWT-based
     * authentication.
     */
    public SecurityConfig(JWTAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configures the security filter chain for HTTP requests. Disables CSRF for
     * stateless APIs, sets session management to stateless, defines public and
     * secured endpoints, and adds the JWT authentication filter.
     *
     * @param http The HttpSecurity object to configure security settings.
     * @return SecurityFilterChain The configured security filter chain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/users/signup", "/api/v1/users/signin").permitAll()
                    .anyRequest().authenticated())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            logger.info("Security filter chain configured successfully with stateless session and JWT authentication");
            return http.build();
        } catch (Exception e) {
            logger.error("Error configuring security filter chain: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to configure security filter chain", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
