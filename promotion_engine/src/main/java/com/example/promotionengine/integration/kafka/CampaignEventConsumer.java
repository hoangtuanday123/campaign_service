package com.example.promotionengine.integration.kafka;

import com.example.promotionengine.client.campaign.CampaignServiceClient;
import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.dto.event.CampaignEvent;
import com.example.promotionengine.service.CampaignCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CampaignEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventConsumer.class);

    private final CampaignServiceClient campaignServiceClient;
    private final CampaignCacheService campaignCacheService;

    public CampaignEventConsumer(
            CampaignServiceClient campaignServiceClient,
            CampaignCacheService campaignCacheService
    ) {
        this.campaignServiceClient = campaignServiceClient;
        this.campaignCacheService = campaignCacheService;
    }

    @KafkaListener(
            topics = "${campaign.events.topic:campaign-events}",
            groupId = "${campaign.events.consumer-group:promotion-engine}"
    )
    public void consume(CampaignEvent event) {
        if (event == null || event.data() == null || event.data().campaignId() == null) {
            log.warn("Ignoring malformed campaign event: {}", event);
            return;
        }

        UUID campaignId = event.data().campaignId();
        String eventType = event.eventType();

        if (CampaignEventType.CAMPAIGN_ACTIVATED.value().equals(eventType)) {
            handleActivated(campaignId);
            return;
        }
        if (CampaignEventType.CAMPAIGN_DEACTIVATED.value().equals(eventType)) {
            campaignCacheService.evictCampaign(campaignId);
            log.info("Evicted cached campaign {} after deactivation event", campaignId);
            return;
        }

        log.debug("Ignoring campaign event type {} for campaign {}", eventType, campaignId);
    }

    private void handleActivated(UUID campaignId) {
        Optional<CampaignDetails> campaign = campaignServiceClient.getCampaign(campaignId);
        if (campaign.isEmpty()) {
            throw new IllegalStateException("Activated campaign could not be loaded for cache warmup: " + campaignId);
        }

        campaignCacheService.cacheCampaign(campaign.get());
        log.info("Cached campaign {} immediately after activation event", campaignId);
    }
}