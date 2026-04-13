package com.example.campaignservice.security;

import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyService(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    public boolean isValidApiKey(String apiKey) {
        return apiKeyProperties.value() != null && apiKeyProperties.value().equals(apiKey);
    }

    public String getApiKeyHeaderName() {
        return apiKeyProperties.headerName() != null ? apiKeyProperties.headerName() : "X-API-Key";
    }
}
