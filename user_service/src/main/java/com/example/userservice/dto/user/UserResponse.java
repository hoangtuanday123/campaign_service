package com.example.userservice.dto.user;

import com.example.userservice.domain.enums.Role;
import com.example.userservice.domain.enums.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String phoneNumber,
        UserStatus status,
        Set<Role> roles,
        Instant createdAt,
        Instant updatedAt
) {
}
