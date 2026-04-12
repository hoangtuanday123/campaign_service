package com.example.promotionengine.client.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.user")
public class UserServiceProperties {

    private String baseUrl;
    private String userByIdPath = "/api/v1/users/{id}";
    private String authToken;

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

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}