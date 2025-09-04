package com.jayatheerth.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jayatheerth.userservice.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing User entities in the database.
 * This interface extends JpaRepository to provide standard CRUD operations
 * and includes custom query methods for user lookup by email.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     * Returns an Optional containing the User if found, or an empty Optional if no
     * user exists with the given email.
     *
     * @param email The email address to search for.
     * @return Optional<User> containing the user if found, otherwise empty.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     * Returns true if a user with the specified email exists, false otherwise.
     *
     * @param email The email address to check.
     * @return boolean indicating whether a user with the given email exists.
     */
    boolean existsByEmail(String email);
}