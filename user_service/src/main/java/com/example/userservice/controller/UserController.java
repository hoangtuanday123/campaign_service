package com.example.userservice.controller;

import com.example.userservice.common.api.ApiResponse;
import com.example.userservice.dto.user.UpdateUserRequest;
import com.example.userservice.dto.user.UserResponse;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User retrieved successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Current user retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable UUID id) {
        userService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User marked as inactive"));
    }
}
