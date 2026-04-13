package com.example.analyticsservice.consumer;

import com.example.analyticsservice.dto.event.CampaignEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CampaignEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(CampaignEventConsumer.class);

    private final Map<String, AnalyticsEventHandler> handlers;

    public CampaignEventConsumer(List<AnalyticsEventHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(AnalyticsEventHandler::supportsEventType, Function.identity()));
    }

    @KafkaListener(
            topics = "${campaign.events.topic:campaign-events}",
            groupId = "${campaign.events.consumer-group:analytics-service}",
            containerFactory = "campaignEventKafkaListenerContainerFactory"
    )
    public void consume(CampaignEvent event) {
        if (event == null || event.eventId() == null || event.data() == null || event.data().campaignId() == null) {
            log.warn("Ignoring malformed campaign event: {}", event);
            return;
        }

        log.info("Received campaign event {} for campaign {}", event.eventType(), event.data().campaignId());

        // Dispatch by event type so future tracking events can be added without rewriting the listener itself.
        AnalyticsEventHandler handler = handlers.get(event.eventType());
        if (handler == null) {
            log.debug("Ignoring unsupported campaign event type {} for campaign {}", event.eventType(), event.data().campaignId());
            return;
        }

        handler.handle(event);
    }
}