package com.example.promotionengine.integration.kafka;

import com.example.promotionengine.client.campaign.CampaignServiceClient;
import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.dto.campaign.CampaignStatus;
import com.example.promotionengine.dto.event.CampaignEvent;
import com.example.promotionengine.dto.event.CampaignEventData;
import com.example.promotionengine.service.CampaignCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignEventConsumerTest {

    @Mock
    private CampaignServiceClient campaignServiceClient;

    @Mock
    private CampaignCacheService campaignCacheService;

    @Test
    void shouldCacheCampaignWhenActivationEventArrives() {
        UUID campaignId = UUID.randomUUID();
        CampaignDetails campaign = activeCampaign(campaignId);
        CampaignEventConsumer consumer = new CampaignEventConsumer(campaignServiceClient, campaignCacheService);

        when(campaignServiceClient.getCampaign(campaignId)).thenReturn(Optional.of(campaign));

        consumer.consume(event(CampaignEventType.CAMPAIGN_ACTIVATED.value(), campaignId));

        verify(campaignCacheService).cacheCampaign(campaign);
    }

    @Test
    void shouldEvictCampaignWhenDeactivationEventArrives() {
        UUID campaignId = UUID.randomUUID();
        CampaignEventConsumer consumer = new CampaignEventConsumer(campaignServiceClient, campaignCacheService);

        consumer.consume(event(CampaignEventType.CAMPAIGN_DEACTIVATED.value(), campaignId));

        verify(campaignCacheService).evictCampaign(campaignId);
        verify(campaignServiceClient, never()).getCampaign(campaignId);
    }

    @Test
    void shouldFailWhenActivatedCampaignCannotBeLoaded() {
        UUID campaignId = UUID.randomUUID();
        CampaignEventConsumer consumer = new CampaignEventConsumer(campaignServiceClient, campaignCacheService);

        when(campaignServiceClient.getCampaign(campaignId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> consumer.consume(event(CampaignEventType.CAMPAIGN_ACTIVATED.value(), campaignId)));
    }

    private CampaignEvent event(String eventType, UUID campaignId) {
        return new CampaignEvent(
                UUID.randomUUID(),
                eventType,
                Instant.parse("2026-04-12T00:00:00Z"),
                new CampaignEventData(campaignId)
        );
    }

    private CampaignDetails activeCampaign(UUID campaignId) {
        return new CampaignDetails(
                campaignId,
                "Spring sale",
                CampaignStatus.ACTIVE,
                Instant.parse("2026-04-11T00:00:00Z"),
                Instant.parse("2026-04-13T00:00:00Z"),
                100,
                20,
                80,
                "RULE-1",
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-01T00:00:00Z")
        );
    }
}