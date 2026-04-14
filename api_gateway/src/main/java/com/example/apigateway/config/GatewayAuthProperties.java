package com.example.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.auth")
public record GatewayAuthProperties(
        String forwardedUserIdHeader,
        String forwardedUsernameHeader
) {
}
