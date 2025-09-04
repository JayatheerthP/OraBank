package com.jayatheerth.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jayatheerth.userservice.exception.UserServiceException;

/**
 * Configuration class for setting up password encoding.
 * This class provides a bean for password encoding using BCrypt, which is a
 * secure
 * hashing algorithm for storing passwords.
 */
@Configuration
public class PasswordEncoderConfig {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncoderConfig.class);

    /**
     * Creates a PasswordEncoder bean using BCryptPasswordEncoder.
     * BCrypt is used for secure password hashing with a configurable strength
     * factor.
     *
     * @return PasswordEncoder instance for encoding and verifying passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        try {
            // Using strength of 12 for BCrypt (higher values increase security but slow
            // down encoding)
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            logger.info("PasswordEncoder bean created successfully with BCrypt strength 12");
            return encoder;
        } catch (Exception e) {
            logger.error("Error creating PasswordEncoder: {}", e.getMessage(), e);
           throw new UserServiceException("Failed to create PasswordEncoder", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}