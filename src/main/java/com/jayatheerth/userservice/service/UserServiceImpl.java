package com.jayatheerth.userservice.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayatheerth.userservice.dto.request.UserSignInRequest;
import com.jayatheerth.userservice.dto.request.UserSignUpRequest;
import com.jayatheerth.userservice.dto.response.UserResponse;
import com.jayatheerth.userservice.dto.response.UserSignInResponse;
import com.jayatheerth.userservice.dto.response.UserSignUpResponse;
import com.jayatheerth.userservice.dto.response.UserStatusResponse;
import com.jayatheerth.userservice.dto.response.WelcomeNotification;
import com.jayatheerth.userservice.entity.User;
import com.jayatheerth.userservice.exception.UserServiceException;
import com.jayatheerth.userservice.repository.UserRepository;
import com.jayatheerth.userservice.security.JWTService;

import lombok.AllArgsConstructor;

/**
 * Implementation of the UserService interface for handling user-related
 * business logic. This service manages user signup, signin, and retrieval
 * operations, including password encoding, JWT token generation, and sending
 * welcome notifications via Kafka.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Registers a new user with the provided details. Checks for existing
     * users, encodes the password, saves the user to the database, and sends a
     * welcome notification.
     *
     * @param request The signup request containing user information.
     * @return UserSignUpResponse The response containing details of the newly
     * created user.
     * @throws UserServiceException If a user with the provided email already
     * exists.
     */
    @Override
    public UserSignUpResponse signUp(UserSignUpRequest request) {
        try {
            logger.debug("Processing signup for email: {}", request.getEmail());
            // Check if user already exists by email
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.error("Signup failed: User already exists with email: {}", request.getEmail());
                throw new UserServiceException("User with this email already exists", HttpStatus.CONFLICT);
            }

            // Map request to User entity
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword())); // Encode the password
            user.setPhoneNumber(request.getPhoneNumber());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setDateOfBirth(request.getDateOfBirth());
            user.setAddress(request.getAddress());
            user.setIsActive(true); // Default to active
            user.setIsLocked(false); // Default to not locked
            user.setFailedLoginAttempts(0); // Initialize failed login attempts

            // Save user to database
            User savedUser = userRepository.save(user);
            logger.info("User successfully saved with userId: {}", savedUser.getUserId());

            // Send welcome notification
            sendWelcomeNotification(savedUser.getEmail(), savedUser.getFirstName());

            // Map saved user to response DTO
            return new UserSignUpResponse(
                    savedUser.getUserId(),
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    savedUser.getLastName(),
                    savedUser.getIsActive(),
                    savedUser.getIsLocked(),
                    savedUser.getCreatedAt());
        } catch (UserServiceException e) {
            logger.error("UserServiceException during signup for email: {}. Error: {}", request.getEmail(),
                    e.getMessage(), e);
            throw e; // Re-throw custom exception to be handled by controller
        } catch (Exception e) {
            logger.error("Unexpected error during signup for email: {}. Error: {}", request.getEmail(), e.getMessage(),
                    e);
            throw new UserServiceException("Unexpected error during user signup for email: " + request.getEmail(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends a welcome notification to the user via Kafka. Converts the
     * notification data to JSON and publishes it to a Kafka topic.
     *
     * @param email The email address of the user.
     * @param firstName The first name of the user for personalization.
     */
    private void sendWelcomeNotification(String email, String firstName) {
        try {
            WelcomeNotification dto = new WelcomeNotification(email, firstName);
            String message = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send("welcome-topic", message);
            logger.info("Sent welcome notification to Kafka for user: {}", email);
        } catch (JsonProcessingException e) {
            logger.error("Failed to send welcome notification to Kafka for user: {}. Error: {}", email, e.getMessage(),
                    e);
            // Do not throw exception to avoid disrupting signup process
        } catch (Exception e) {
            logger.error("Unexpected error sending welcome notification for user: {}. Error: {}", email, e.getMessage(), e);
            // Do not throw exception to avoid disrupting signup process
        }
    }

    /**
     * Authenticates a user based on provided credentials. Validates email and
     * password, checks account status, and generates a JWT token upon
     * successful login.
     *
     * @param request The signin request containing user credentials.
     * @return UserSignInResponse The response containing authentication details
     * like token.
     * @throws UserServiceException If the user is not found, credentials are
     * invalid, or the account is locked.
     */
    @Override
    public UserSignInResponse signIn(UserSignInRequest request) {
        try {
            logger.debug("Processing signin for email: {}", request.getEmail());
            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        logger.error("Signin failed: User not found with email: {}", request.getEmail());
                        throw new UserServiceException("User not found", HttpStatus.NOT_FOUND);
                    });

            // Check if account is locked
            if (user.getIsLocked()) {
                logger.error("Signin failed: Account locked for email: {}", request.getEmail());
                throw new UserServiceException("Account is locked", HttpStatus.FORBIDDEN);
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // Increment failed login attempts
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                // Lock account after a threshold (e.g., 5 failed attempts)
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setIsLocked(true);
                    userRepository.save(user);
                    throw new UserServiceException("Account is locked", HttpStatus.FORBIDDEN);

                }
                userRepository.save(user);
                logger.error("Signin failed: Invalid credentials for email: {}", request.getEmail());
                throw new UserServiceException("Invalid email or password", HttpStatus.UNAUTHORIZED);
            }

            // Reset failed login attempts on successful login
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            // Generate JWT token
            String token = jwtService.generateToken(user.getUserId(), user.getEmail(), user.getFirstName());
            logger.info("Signin successful for userId: {}, token generated", user.getUserId());

            // Return sign-in response with token and expiration time
            return new UserSignInResponse(token, user.getUserId(), jwtService.getExpirationTime());
        } catch (UserServiceException e) {
            logger.error("UserServiceException during signin for email: {}. Error: {}", request.getEmail(),
                    e.getMessage(), e);
            throw e; // Re-throw custom exception to be handled by controller
        } catch (Exception e) {
            logger.error("Unexpected error during signin for email: {}. Error: {}", request.getEmail(), e.getMessage(),
                    e);
            throw new UserServiceException("Unexpected error during user signin for email: " + request.getEmail(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * Retrieves detailed information about a user based on their ID.
     *
     * @param userId The UUID of the user to retrieve.
     * @return UserResponse The response containing user details.
     * @throws UserServiceException If the user is not found.
     */
    @Override
    public UserResponse getUser(UUID userId) {
        try {
            logger.debug("Fetching user details for userId: {}", userId);
            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("User not found with userId: {}", userId);
                        throw new UserServiceException("User not found", HttpStatus.NOT_FOUND);
                    });

            logger.info("User details retrieved for userId: {}", userId);

            // Map user entity to response DTO
            return new UserResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getDateOfBirth(),
                    user.getAddress(),
                    user.getIsActive(),
                    user.getIsLocked(),
                    user.getCreatedAt());
        } catch (UserServiceException e) {
            logger.error("UserServiceException fetching user details for userId: {}. Error: {}", userId, e.getMessage(),
                    e);
            throw e; // Re-throw custom exception to be handled by controller
        } catch (Exception e) {
            logger.error("Unexpected error fetching user details for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserServiceException("Unexpected error fetching user details for userId: " + userId, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * Retrieves the status of a user based on their ID.
     *
     * @param userId The UUID of the user to check status for.
     * @return UserStatusResponse The response containing user status
     * information.
     * @throws UserServiceException If the user is not found.
     */
    @Override
    public UserStatusResponse getStatus(UUID userId) {
        try {
            logger.debug("Fetching user status for userId: {}", userId);
            // Find user by ID
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("User not found with userId: {}", userId);
                        throw new UserServiceException("User not found", HttpStatus.NOT_FOUND);
                    });

            logger.info("User status retrieved for userId: {}", userId);

            // Map user entity to status response DTO
            return new UserStatusResponse(
                    user.getUserId(),
                    user.getIsActive(),
                    user.getIsLocked(),
                    user.getFailedLoginAttempts());
        } catch (UserServiceException e) {
            logger.error("UserServiceException fetching user status for userId: {}. Error: {}", userId, e.getMessage(),
                    e);
            throw e; // Re-throw custom exception to be handled by controller
        } catch (Exception e) {
            logger.error("Unexpected error fetching user status for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserServiceException("Unexpected error fetching user status for userId: " + userId, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
