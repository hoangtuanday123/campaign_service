package com.example.campaignservice.integration.kafka;

public enum CampaignEventType {
    CAMPAIGN_CREATED("campaign_created"),
    CAMPAIGN_ACTIVATED("campaign_activated"),
    CAMPAIGN_DEACTIVATED("campaign_deactivated");

    private final String value;

    CampaignEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}