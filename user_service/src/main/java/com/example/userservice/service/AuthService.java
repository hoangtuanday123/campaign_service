package com.example.userservice.service;

import com.example.userservice.dto.auth.AuthResponse;
import com.example.userservice.dto.auth.LoginRequest;
import com.example.userservice.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
