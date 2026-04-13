package com.example.analyticsservice.service.impl;

import com.example.analyticsservice.domain.document.CampaignAnalytics;
import com.example.analyticsservice.domain.enums.AnalyticsStatus;
import com.example.analyticsservice.exception.AnalyticsReportNotReadyException;
import com.example.analyticsservice.repository.AnalyticsMetric;
import com.example.analyticsservice.repository.CampaignAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private CampaignAnalyticsRepository campaignAnalyticsRepository;

    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(campaignAnalyticsRepository);
    }

    @Test
    void getCampaignAnalyticsShouldReturnMappedResponse() {
        UUID campaignId = UUID.randomUUID();
        CampaignAnalytics analytics = analytics(campaignId, AnalyticsStatus.ACTIVE);

        when(campaignAnalyticsRepository.findById(campaignId)).thenReturn(Optional.of(analytics));

        var response = analyticsService.getCampaignAnalytics(campaignId);

        assertThat(response.campaignId()).isEqualTo(campaignId);
        assertThat(response.impressions()).isEqualTo(100);
        assertThat(response.clicks()).isEqualTo(25);
        assertThat(response.conversions()).isEqualTo(4);
    }

    @Test
    void getCampaignReportShouldRejectActiveAnalytics() {
        UUID campaignId = UUID.randomUUID();
        CampaignAnalytics analytics = analytics(campaignId, AnalyticsStatus.ACTIVE);

        when(campaignAnalyticsRepository.findById(campaignId)).thenReturn(Optional.of(analytics));

        assertThatThrownBy(() -> analyticsService.getCampaignReport(campaignId))
                .isInstanceOf(AnalyticsReportNotReadyException.class)
                .hasMessageContaining("not ready");
    }

    @Test
    void trackConversionShouldIncrementMetricWhenCampaignIsActive() {
        UUID eventId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        CampaignAnalytics analytics = analytics(campaignId, AnalyticsStatus.ACTIVE);

        when(campaignAnalyticsRepository.incrementMetric(eventId, campaignId, AnalyticsMetric.CONVERSIONS, Instant.parse("2026-04-13T08:40:00Z")))
                .thenReturn(Optional.of(analytics));

        analyticsService.trackConversion(eventId, campaignId, Instant.parse("2026-04-13T08:40:00Z"));

        verify(campaignAnalyticsRepository).incrementMetric(eventId, campaignId, AnalyticsMetric.CONVERSIONS, Instant.parse("2026-04-13T08:40:00Z"));
    }

    private CampaignAnalytics analytics(UUID campaignId, AnalyticsStatus status) {
        return CampaignAnalytics.builder()
                .campaignId(campaignId)
                .impressions(100)
                .clicks(25)
                .conversions(4)
                .status(status)
                .startedAt(Instant.parse("2026-04-13T08:30:00Z"))
                .endedAt(status == AnalyticsStatus.COMPLETED ? Instant.parse("2026-04-13T10:00:00Z") : null)
                .processedEventIds(Set.of(UUID.randomUUID()))
                .build();
    }
}