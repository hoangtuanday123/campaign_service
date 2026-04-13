package com.example.analyticsservice.dto.analytics;

import com.example.analyticsservice.domain.document.CampaignAnalytics;
import com.example.analyticsservice.domain.enums.AnalyticsStatus;

import java.time.Instant;
import java.util.UUID;

public record CampaignAnalyticsResponse(
        UUID campaignId,
        long impressions,
        long clicks,
        long conversions,
        AnalyticsStatus status,
        Instant startedAt,
        Instant endedAt
) {

    public static CampaignAnalyticsResponse from(CampaignAnalytics analytics) {
        return new CampaignAnalyticsResponse(
                analytics.getCampaignId(),
                analytics.getImpressions(),
                analytics.getClicks(),
                analytics.getConversions(),
                analytics.getStatus(),
                analytics.getStartedAt(),
                analytics.getEndedAt()
        );
    }
}