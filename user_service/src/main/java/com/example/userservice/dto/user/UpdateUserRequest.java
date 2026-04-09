package com.example.userservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 50) String username,
        @Email String email,
        @Pattern(regexp = "^$|^[+]?[0-9]{8,15}$", message = "phoneNumber must be a valid phone number") String phoneNumber
) {
}
