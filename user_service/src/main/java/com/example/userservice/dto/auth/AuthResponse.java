package com.example.userservice.dto.auth;

import com.example.userservice.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
