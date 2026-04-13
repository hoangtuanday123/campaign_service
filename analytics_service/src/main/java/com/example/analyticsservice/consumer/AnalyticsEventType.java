package com.example.analyticsservice.consumer;

public enum AnalyticsEventType {
    CAMPAIGN_ACTIVATED("campaign_activated"),
    CAMPAIGN_DEACTIVATED("campaign_deactivated"),
    CAMPAIGN_VIEWED("campaign_viewed"),
    CAMPAIGN_CLICKED("campaign_clicked"),
    PROMOTION_APPLIED("promotion_applied");

    private final String value;

    AnalyticsEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}