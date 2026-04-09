package com.example.campaignservice.integration.kafka;

import com.example.campaignservice.config.CampaignEventsProperties;
import com.example.campaignservice.dto.event.CampaignEvent;
import com.example.campaignservice.dto.event.CampaignEventData;
import com.example.campaignservice.service.CampaignEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaCampaignEventPublisher implements CampaignEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaCampaignEventPublisher.class);

    private final KafkaTemplate<String, CampaignEvent> kafkaTemplate;
    private final CampaignEventsProperties campaignEventsProperties;

    public KafkaCampaignEventPublisher(
            KafkaTemplate<String, CampaignEvent> kafkaTemplate,
            CampaignEventsProperties campaignEventsProperties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.campaignEventsProperties = campaignEventsProperties;
    }

    @Override
    public void publishCreated(UUID campaignId) {
        publish(CampaignEventType.CAMPAIGN_CREATED, campaignId);
    }

    @Override
    public void publishActivated(UUID campaignId) {
        publish(CampaignEventType.CAMPAIGN_ACTIVATED, campaignId);
    }

    @Override
    public void publishDeactivated(UUID campaignId) {
        publish(CampaignEventType.CAMPAIGN_DEACTIVATED, campaignId);
    }

    private void publish(CampaignEventType eventType, UUID campaignId) {
        CampaignEvent event = new CampaignEvent(
                UUID.randomUUID(),
                eventType.value(),
                Instant.now(),
                new CampaignEventData(campaignId)
        );

        kafkaTemplate.send(campaignEventsProperties.getTopic(), campaignId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} event for campaign {}", eventType.value(), campaignId, ex);
                        return;
                    }
                    log.info("Published {} event for campaign {} to topic {}", eventType.value(), campaignId, campaignEventsProperties.getTopic());
                });
    }
}