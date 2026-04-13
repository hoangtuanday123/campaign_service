package com.example.analyticsservice.consumer;

import com.example.analyticsservice.dto.event.CampaignEvent;
import com.example.analyticsservice.service.AnalyticsService;
import org.springframework.stereotype.Component;

@Component
public class CampaignDeactivatedEventHandler implements AnalyticsEventHandler {

    private final AnalyticsService analyticsService;

    public CampaignDeactivatedEventHandler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public String supportsEventType() {
        return AnalyticsEventType.CAMPAIGN_DEACTIVATED.value();
    }

    @Override
    public void handle(CampaignEvent event) {
        analyticsService.completeTracking(event.eventId(), event.data().campaignId(), event.timestamp());
    }
}