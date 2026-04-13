package com.example.promotionengine.dto.event;

public enum PromotionEventType {

    PROMOTION_APPLIED("promotion_applied"),
    CAMPAIGN_VIEWED("campaign_viewed"),
    CAMPAIGN_CLICKED("campaign_clicked");

    private final String value;

    PromotionEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}