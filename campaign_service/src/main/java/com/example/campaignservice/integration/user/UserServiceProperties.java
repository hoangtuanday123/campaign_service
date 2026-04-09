package com.example.campaignservice.integration.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.user")
public class UserServiceProperties {

    private String baseUrl;
    private String userByIdPath = "/api/users/{id}";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserByIdPath() {
        return userByIdPath;
    }

    public void setUserByIdPath(String userByIdPath) {
        this.userByIdPath = userByIdPath;
    }
}