package com.jayatheerth.userservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusResponse {

    private UUID userId;
    private Boolean isActive;
    private Boolean isLocked;
    private Integer failedLoginAttempts;
}