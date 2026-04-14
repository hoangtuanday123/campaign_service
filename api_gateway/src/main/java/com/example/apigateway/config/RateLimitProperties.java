package com.example.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.rate-limit")
public record RateLimitProperties(
        int maxRequests,
        long windowSeconds,
        String keyPrefix
) {
}
