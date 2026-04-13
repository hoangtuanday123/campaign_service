package com.example.userservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.apikey")
public record ApiKeyProperties(
        String header,
        String value
) {
}
