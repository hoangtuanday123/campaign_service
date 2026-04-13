package com.example.analyticsservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.api-key")
public record ApiKeyProperties(
        String headerName,
        String value
) {
}