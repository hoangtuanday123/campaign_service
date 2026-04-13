package com.example.analyticsservice.consumer;

import com.example.analyticsservice.dto.event.CampaignEvent;
import com.example.analyticsservice.service.AnalyticsService;
import org.springframework.stereotype.Component;

@Component
public class CampaignActivatedEventHandler implements AnalyticsEventHandler {

    private final AnalyticsService analyticsService;

    public CampaignActivatedEventHandler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public String supportsEventType() {
        return AnalyticsEventType.CAMPAIGN_ACTIVATED.value();
    }

    @Override
    public void handle(CampaignEvent event) {
        analyticsService.startTracking(event.eventId(), event.data().campaignId(), event.timestamp());
    }
}