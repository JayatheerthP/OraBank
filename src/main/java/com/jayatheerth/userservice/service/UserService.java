package com.jayatheerth.userservice.service;

import java.util.UUID;

import com.jayatheerth.userservice.dto.request.UserSignInRequest;
import com.jayatheerth.userservice.dto.request.UserSignUpRequest;
import com.jayatheerth.userservice.dto.response.UserResponse;
import com.jayatheerth.userservice.dto.response.UserSignInResponse;
import com.jayatheerth.userservice.dto.response.UserSignUpResponse;
import com.jayatheerth.userservice.dto.response.UserStatusResponse;

/**
 * Interface defining the contract for user service operations.
 * This service handles user-related business logic such as signup, signin,
 * and retrieval of user details and status.
 */
public interface UserService {

    /**
     * Registers a new user with the provided details.
     *
     * @param request The signup request containing user information.
     * @return UserSignUpResponse The response containing details of the newly
     *         created user.
     */
    UserSignUpResponse signUp(UserSignUpRequest request);

    /**
     * Authenticates a user based on provided credentials.
     *
     * @param request The signin request containing user credentials.
     * @return UserSignInResponse The response containing authentication details
     *         like token.
     */
    UserSignInResponse signIn(UserSignInRequest request);

    /**
     * Retrieves detailed information about a user based on their ID.
     *
     * @param userId The UUID of the user to retrieve.
     * @return UserResponse The response containing user details.
     */
    UserResponse getUser(UUID userId);

    /**
     * Retrieves the status of a user based on their ID.
     *
     * @param userId The UUID of the user to check status for.
     * @return UserStatusResponse The response containing user status information.
     */
    UserStatusResponse getStatus(UUID userId);
}