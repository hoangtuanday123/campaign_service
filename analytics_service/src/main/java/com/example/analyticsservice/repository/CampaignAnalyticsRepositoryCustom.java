package com.example.analyticsservice.repository;

import com.example.analyticsservice.domain.document.CampaignAnalytics;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CampaignAnalyticsRepositoryCustom {

    Optional<CampaignAnalytics> initializeTracking(UUID eventId, UUID campaignId, Instant startedAt);

    Optional<CampaignAnalytics> completeTracking(UUID eventId, UUID campaignId, Instant endedAt);

    Optional<CampaignAnalytics> incrementMetric(UUID eventId, UUID campaignId, AnalyticsMetric metric, Instant occurredAt);
}