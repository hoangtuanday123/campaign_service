package com.example.userservice.service;

import com.example.userservice.dto.user.UpdateUserRequest;
import com.example.userservice.dto.user.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getById(UUID id);

    UserResponse getCurrentUser();

    UserResponse update(UUID id, UpdateUserRequest request);

    void softDelete(UUID id);
}
