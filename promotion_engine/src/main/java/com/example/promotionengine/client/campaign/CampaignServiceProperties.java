package com.example.promotionengine.client.campaign;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.campaign")
public class CampaignServiceProperties {

    private String baseUrl;
    private String campaignByIdPath = "/api/v1/campaigns/{id}";
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCampaignByIdPath() {
        return campaignByIdPath;
    }

    public void setCampaignByIdPath(String campaignByIdPath) {
        this.campaignByIdPath = campaignByIdPath;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}