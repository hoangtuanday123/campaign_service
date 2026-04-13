package com.example.analyticsservice.service;

import com.example.analyticsservice.dto.analytics.CampaignAnalyticsResponse;

import java.time.Instant;
import java.util.UUID;

public interface AnalyticsService {

    CampaignAnalyticsResponse getCampaignAnalytics(UUID campaignId);

    CampaignAnalyticsResponse getCampaignReport(UUID campaignId);

    void startTracking(UUID eventId, UUID campaignId, Instant startedAt);

    void completeTracking(UUID eventId, UUID campaignId, Instant endedAt);

    void trackImpression(UUID eventId, UUID campaignId, Instant occurredAt);

    void trackClick(UUID eventId, UUID campaignId, Instant occurredAt);

    void trackConversion(UUID eventId, UUID campaignId, Instant occurredAt);
}