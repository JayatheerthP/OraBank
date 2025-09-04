package com.jayatheerth.userservice.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jayatheerth.userservice.dto.request.UserSignInRequest;
import com.jayatheerth.userservice.dto.request.UserSignUpRequest;
import com.jayatheerth.userservice.dto.response.UserResponse;
import com.jayatheerth.userservice.dto.response.UserSignInResponse;
import com.jayatheerth.userservice.dto.response.UserSignUpResponse;
import com.jayatheerth.userservice.dto.response.UserStatusResponse;
import com.jayatheerth.userservice.exception.UserServiceException;
import com.jayatheerth.userservice.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * REST Controller for handling user-related operations. This controller
 * provides endpoints for user signup, signin, and retrieving user details and
 * status. All operations are logged for monitoring and debugging purposes.
 */
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Handles user signup requests. Validates the incoming request and
     * delegates the signup process to the UserService.
     *
     * @param request The signup request containing user details like email and
     * password.
     * @return ResponseEntity containing the signup response with user details
     * and HTTP status CREATED.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserSignUpResponse> signup(@Valid @RequestBody UserSignUpRequest request) {
        try {
            logger.info("Received signup request for email: {}", request.getEmail());
            UserSignUpResponse response = userService.signUp(request);
            logger.info("User signup successful for email: {}", request.getEmail());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error during signup for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw new UserServiceException("User signup failed for email: " + request.getEmail(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handles user signin requests. Validates the incoming request and
     * delegates the signin process to the UserService.
     *
     * @param request The signin request containing user credentials like email
     * and password.
     * @return ResponseEntity containing the signin response with authentication
     * details and HTTP status OK.
     */
    @PostMapping("/signin")
    public ResponseEntity<UserSignInResponse> signin(@Valid @RequestBody UserSignInRequest request) {
        try {
            logger.info("Received signin request for email: {}", request.getEmail());
            UserSignInResponse response = userService.signIn(request);
            logger.info("User signin successful for email: {}", request.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error during signin for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw new UserServiceException("User signin failed for email: " + request.getEmail(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Retrieves user details based on the provided user ID. This endpoint is
     * secured and requires authentication.
     *
     * @param userId The UUID of the user to retrieve details for.
     * @return ResponseEntity containing the user details and HTTP status OK.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        try {
            logger.info("Received request to get user details for userId: {}", userId);
            UserResponse response = userService.getUser(userId);
            logger.debug("Returning user details for userId: {}", userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user details for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserServiceException("Failed to retrieve user details for userId: " + userId, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves user status based on the provided user ID. This endpoint is
     * secured and requires authentication.
     *
     * @param userId The UUID of the user to retrieve status for.
     * @return ResponseEntity containing the user status and HTTP status OK.
     */
    @GetMapping("/{userId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserStatusResponse> getStatus(@PathVariable UUID userId) {
        try {
            logger.info("Received request to get user status for userId: {}", userId);
            UserStatusResponse response = userService.getStatus(userId);
            logger.debug("Returning user status for userId: {}", userId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user status for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserServiceException("Failed to retrieve user status for userId: " + userId, HttpStatus.NOT_FOUND);
        }
    }
}
