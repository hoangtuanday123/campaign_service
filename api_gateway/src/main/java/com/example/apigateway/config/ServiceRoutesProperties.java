package com.example.apigateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record ServiceRoutesProperties(
        ServiceRoute user,
        ServiceRoute campaign,
        ServiceRoute promotion,
        ServiceRoute analytics
) {

    public record ServiceRoute(String baseUrl) {
    }
}
