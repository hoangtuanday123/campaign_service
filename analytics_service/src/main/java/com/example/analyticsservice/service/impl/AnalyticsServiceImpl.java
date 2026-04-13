package com.example.analyticsservice.service.impl;

import com.example.analyticsservice.domain.document.CampaignAnalytics;
import com.example.analyticsservice.domain.enums.AnalyticsStatus;
import com.example.analyticsservice.dto.analytics.CampaignAnalyticsResponse;
import com.example.analyticsservice.exception.AnalyticsReportNotReadyException;
import com.example.analyticsservice.exception.ResourceNotFoundException;
import com.example.analyticsservice.repository.AnalyticsMetric;
import com.example.analyticsservice.repository.CampaignAnalyticsRepository;
import com.example.analyticsservice.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final CampaignAnalyticsRepository campaignAnalyticsRepository;

    public AnalyticsServiceImpl(CampaignAnalyticsRepository campaignAnalyticsRepository) {
        this.campaignAnalyticsRepository = campaignAnalyticsRepository;
    }

    @Override
    public CampaignAnalyticsResponse getCampaignAnalytics(UUID campaignId) {
        return CampaignAnalyticsResponse.from(getCampaignAnalyticsDocument(campaignId));
    }

    @Override
    public CampaignAnalyticsResponse getCampaignReport(UUID campaignId) {
        CampaignAnalytics analytics = getCampaignAnalyticsDocument(campaignId);
        if (analytics.getStatus() != AnalyticsStatus.COMPLETED) {
            throw new AnalyticsReportNotReadyException("Campaign analytics report is not ready: " + campaignId);
        }
        return CampaignAnalyticsResponse.from(analytics);
    }

    @Override
    public void startTracking(UUID eventId, UUID campaignId, Instant startedAt) {
        campaignAnalyticsRepository.initializeTracking(eventId, campaignId, startedAt)
                .ifPresentOrElse(
                        analytics -> log.info("Initialized analytics tracking for campaign {}", campaignId),
                        () -> log.info("Skipping duplicate activation event {} for campaign {}", eventId, campaignId)
                );
    }

    @Override
    public void completeTracking(UUID eventId, UUID campaignId, Instant endedAt) {
        if (campaignAnalyticsRepository.completeTracking(eventId, campaignId, endedAt).isPresent()) {
            log.info("Completed analytics tracking for campaign {}", campaignId);
            return;
        }

        if (campaignAnalyticsRepository.existsById(campaignId)) {
            log.info("Skipping duplicate deactivation event {} for campaign {}", eventId, campaignId);
            return;
        }

        log.warn("Received deactivation event for unknown campaign analytics {}", campaignId);
    }

    @Override
    public void trackImpression(UUID eventId, UUID campaignId, Instant occurredAt) {
        trackMetric(eventId, campaignId, occurredAt, AnalyticsMetric.IMPRESSIONS, "impression");
    }

    @Override
    public void trackClick(UUID eventId, UUID campaignId, Instant occurredAt) {
        trackMetric(eventId, campaignId, occurredAt, AnalyticsMetric.CLICKS, "click");
    }

    @Override
    public void trackConversion(UUID eventId, UUID campaignId, Instant occurredAt) {
        trackMetric(eventId, campaignId, occurredAt, AnalyticsMetric.CONVERSIONS, "conversion");
    }

    private CampaignAnalytics getCampaignAnalyticsDocument(UUID campaignId) {
        return campaignAnalyticsRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign analytics not found: " + campaignId));
    }

    private void trackMetric(UUID eventId, UUID campaignId, Instant occurredAt, AnalyticsMetric metric, String metricName) {
        if (campaignAnalyticsRepository.incrementMetric(eventId, campaignId, metric, occurredAt).isPresent()) {
            log.info("Recorded {} for campaign {}", metricName, campaignId);
            return;
        }

        CampaignAnalytics analytics = campaignAnalyticsRepository.findById(campaignId).orElse(null);
        if (analytics == null) {
            log.warn("Ignoring {} event {} because campaign analytics {} does not exist yet", metricName, eventId, campaignId);
            return;
        }
        if (analytics.getStatus() == AnalyticsStatus.COMPLETED) {
            log.info("Ignoring {} event {} because campaign {} analytics is already completed", metricName, eventId, campaignId);
            return;
        }
        log.info("Skipping duplicate {} event {} for campaign {}", metricName, eventId, campaignId);
    }
}