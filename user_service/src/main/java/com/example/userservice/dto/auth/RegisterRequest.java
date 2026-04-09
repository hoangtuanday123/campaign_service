package com.example.userservice.dto.auth;

import com.example.userservice.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @Pattern(regexp = "^$|^[+]?[0-9]{8,15}$", message = "phoneNumber must be a valid phone number") String phoneNumber,
        Set<Role> roles
) {
}
