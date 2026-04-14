package com.example.apigateway.common.api;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        int status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(Instant.now(), status, message, data);
    }

    public static ApiResponse<Void> success(int status, String message) {
        return success(status, message, null);
    }
}
