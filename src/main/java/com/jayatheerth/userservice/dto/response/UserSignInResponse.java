package com.jayatheerth.userservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignInResponse {

    private String token;
    private UUID userId;
    private long expiresIn;
}