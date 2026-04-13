package com.example.promotionengine.service.impl;

import com.example.promotionengine.client.campaign.CampaignServiceClient;
import com.example.promotionengine.client.user.UserServiceClient;
import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.dto.campaign.CampaignStatus;
import com.example.promotionengine.dto.event.PromotionEvent;
import com.example.promotionengine.dto.promotion.ApplyPromotionRequest;
import com.example.promotionengine.dto.promotion.PromotionEligibilityResponse;
import com.example.promotionengine.dto.promotion.PromotionInteractionRequest;
import com.example.promotionengine.dto.user.UserSnapshot;
import com.example.promotionengine.producer.PromotionEventProducer;
import com.example.promotionengine.service.CampaignCacheService;
import com.example.promotionengine.service.PromotionUsageClaimResult;
import com.example.promotionengine.service.PromotionUsageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private CampaignServiceClient campaignServiceClient;

    @Mock
    private CampaignCacheService campaignCacheService;

    @Mock
    private PromotionUsageService promotionUsageService;

    @Mock
    private PromotionEventProducer promotionEventProducer;

    private PromotionServiceImpl promotionService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-04-12T00:00:00Z"), ZoneOffset.UTC);
        promotionService = new PromotionServiceImpl(
                userServiceClient,
                campaignServiceClient,
                campaignCacheService,
                promotionUsageService,
                promotionEventProducer,
                clock
        );
    }

    @Test
    void shouldApplyPromotionWhenUserAndCampaignAreEligible() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        ApplyPromotionRequest request = new ApplyPromotionRequest(userId, campaignId);
        CampaignDetails campaign = activeCampaign(campaignId);

        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(new UserSnapshot(userId, "ACTIVE")));
        when(campaignCacheService.getCampaign(campaignId)).thenReturn(Optional.of(campaign));
        when(promotionUsageService.claimUsage(campaignId, userId, campaign.endTime()))
                .thenReturn(PromotionUsageClaimResult.CLAIMED);

        PromotionEligibilityResponse response = promotionService.apply(request);

        assertEquals(true, response.eligible());
        assertEquals("Promotion applied successfully", response.message());
        ArgumentCaptor<PromotionEvent> eventCaptor = ArgumentCaptor.forClass(PromotionEvent.class);
        verify(promotionEventProducer).sendEvent(eventCaptor.capture());
        assertEquals("promotion_applied", eventCaptor.getValue().eventType());
        assertEquals(userId, eventCaptor.getValue().userId());
        assertEquals(campaignId, eventCaptor.getValue().campaignId());
        assertEquals(Instant.parse("2026-04-12T00:00:00Z"), eventCaptor.getValue().timestamp());
        verify(campaignServiceClient, never()).getCampaign(campaignId);
    }

    @Test
    void shouldRejectWhenCampaignIsExpired() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        ApplyPromotionRequest request = new ApplyPromotionRequest(userId, campaignId);
        CampaignDetails expiredCampaign = new CampaignDetails(
                campaignId,
                "Spring sale",
                CampaignStatus.ACTIVE,
                Instant.parse("2026-04-10T00:00:00Z"),
                Instant.parse("2026-04-11T00:00:00Z"),
                100,
                20,
                80,
                "RULE-1",
                Instant.parse("2026-04-01T00:00:00Z"),
                Instant.parse("2026-04-01T00:00:00Z")
        );

        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(new UserSnapshot(userId, "ACTIVE")));
        when(campaignCacheService.getCampaign(campaignId)).thenReturn(Optional.of(expiredCampaign));

        PromotionEligibilityResponse response = promotionService.apply(request);

        assertEquals(false, response.eligible());
        assertEquals("Campaign has expired", response.message());
        verify(promotionUsageService, never()).claimUsage(campaignId, userId, expiredCampaign.endTime());
    }

    @Test
    void shouldRejectWhenUserAlreadyUsedPromotion() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        ApplyPromotionRequest request = new ApplyPromotionRequest(userId, campaignId);
        CampaignDetails campaign = activeCampaign(campaignId);

        when(userServiceClient.getUser(userId)).thenReturn(Optional.of(new UserSnapshot(userId, "ACTIVE")));
        when(campaignCacheService.getCampaign(campaignId)).thenReturn(Optional.of(campaign));
        when(promotionUsageService.claimUsage(campaignId, userId, campaign.endTime()))
                .thenReturn(PromotionUsageClaimResult.ALREADY_USED);

        PromotionEligibilityResponse response = promotionService.apply(request);

        assertEquals(false, response.eligible());
        assertEquals("User already used promotion", response.message());
        verify(promotionEventProducer, never()).sendEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldPublishCampaignViewedEvent() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();

        promotionService.trackCampaignView(new PromotionInteractionRequest(userId, campaignId));

        ArgumentCaptor<PromotionEvent> eventCaptor = ArgumentCaptor.forClass(PromotionEvent.class);
        verify(promotionEventProducer).sendEvent(eventCaptor.capture());
        assertEquals("campaign_viewed", eventCaptor.getValue().eventType());
        assertEquals(userId, eventCaptor.getValue().userId());
        assertEquals(campaignId, eventCaptor.getValue().campaignId());
    }

    @Test
    void shouldPublishCampaignClickedEvent() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();

        promotionService.trackCampaignClick(new PromotionInteractionRequest(userId, campaignId));

        ArgumentCaptor<PromotionEvent> eventCaptor = ArgumentCaptor.forClass(PromotionEvent.class);
        verify(promotionEventProducer).sendEvent(eventCaptor.capture());
        assertEquals("campaign_clicked", eventCaptor.getValue().eventType());
        assertEquals(userId, eventCaptor.getValue().userId());
        assertEquals(campaignId, eventCaptor.getValue().campaignId());
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