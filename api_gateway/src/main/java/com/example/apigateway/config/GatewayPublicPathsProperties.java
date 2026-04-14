package com.example.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gateway")
public record GatewayPublicPathsProperties(
        List<String> publicPaths
) {
}
