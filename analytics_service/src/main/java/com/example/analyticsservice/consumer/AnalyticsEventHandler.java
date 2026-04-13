package com.example.analyticsservice.consumer;

import com.example.analyticsservice.dto.event.CampaignEvent;

public interface AnalyticsEventHandler {

    String supportsEventType();

    void handle(CampaignEvent event);
}