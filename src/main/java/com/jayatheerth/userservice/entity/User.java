package com.jayatheerth.userservice.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a User in the system.
 * This class maps to the 'users' table in the database and contains
 * user-related data
 * such as email, password, and personal details. It includes validation
 * constraints
 * to ensure data integrity and lifecycle hooks for tracking creation and update
 * timestamps.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user, automatically generated as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    /**
     * User's email address, must be unique and valid.
     */
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * User's password, must be at least 8 characters long.
     */
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User's phone number, must match the specified pattern for validity.
     */
    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Phone number is invalid")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    /**
     * User's first name, must be between 2 and 50 characters.
     */
    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * User's last name, must be between 2 and 50 characters.
     */
    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * User's date of birth, must be in the past.
     */
    @NotNull(message = "Date of birth is mandatory")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * User's address, must be between 5 and 255 characters.
     */
    @NotBlank(message = "Address is mandatory")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    @Column(name = "address", nullable = false)
    private String address;

    /**
     * Indicates whether the user account is active.
     */
    @NotNull(message = "Active status is mandatory")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /**
     * Indicates whether the user account is locked.
     */
    @NotNull(message = "Locked status is mandatory")
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    /**
     * Number of failed login attempts, must be non-negative.
     */
    @NotNull(message = "Failed login attempts is mandatory")
    @Min(value = 0, message = "Failed login attempts cannot be negative")
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts;

    /**
     * Timestamp when the user record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle hook to set creation and update timestamps before persisting the
     * entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle hook to update the timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}