package com.example.analyticsservice.controller;

import com.example.analyticsservice.domain.enums.AnalyticsStatus;
import com.example.analyticsservice.dto.analytics.CampaignAnalyticsResponse;
import com.example.analyticsservice.service.AnalyticsService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AnalyticsControllerTest {

    @Test
    void shouldReturnCampaignAnalyticsUsingSharedApiResponseFormat() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        AnalyticsController controller = new AnalyticsController(analyticsService);
        UUID campaignId = UUID.randomUUID();
        CampaignAnalyticsResponse response = new CampaignAnalyticsResponse(
                campaignId,
                1240,
                97,
                11,
                AnalyticsStatus.ACTIVE,
                Instant.parse("2026-04-13T08:30:00Z"),
                null
        );

        when(analyticsService.getCampaignAnalytics(campaignId)).thenReturn(response);

        var entity = controller.getCampaignAnalytics(campaignId);

        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().status()).isEqualTo(200);
        assertThat(entity.getBody().message()).isEqualTo("Campaign analytics retrieved successfully");
        assertThat(entity.getBody().data()).isEqualTo(response);
    }

    @Test
    void shouldReturnCampaignReportUsingSharedApiResponseFormat() {
        AnalyticsService analyticsService = mock(AnalyticsService.class);
        AnalyticsController controller = new AnalyticsController(analyticsService);
        UUID campaignId = UUID.randomUUID();
        CampaignAnalyticsResponse response = new CampaignAnalyticsResponse(
                campaignId,
                1240,
                97,
                11,
                AnalyticsStatus.COMPLETED,
                Instant.parse("2026-04-13T08:30:00Z"),
                Instant.parse("2026-04-13T10:00:00Z")
        );

        when(analyticsService.getCampaignReport(campaignId)).thenReturn(response);

        var entity = controller.getCampaignReport(campaignId);

        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().status()).isEqualTo(200);
        assertThat(entity.getBody().message()).isEqualTo("Campaign analytics report retrieved successfully");
        assertThat(entity.getBody().data()).isEqualTo(response);
    }
}