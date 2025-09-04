package com.jayatheerth.userservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime createdAt;
}