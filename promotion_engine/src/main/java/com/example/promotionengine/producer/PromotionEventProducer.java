package com.example.promotionengine.producer;

import com.example.promotionengine.config.PromotionEventProperties;
import com.example.promotionengine.dto.event.PromotionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Delivery retries are delegated to the Kafka producer configuration so request threads stay non-blocking.
 */
@Component
public class PromotionEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PromotionEventProducer.class);
    private static final String EVENT_TYPE_HEADER = "eventType";

    private final KafkaTemplate<String, PromotionEvent> kafkaTemplate;
    private final PromotionEventProperties properties;

    public PromotionEventProducer(
            KafkaTemplate<String, PromotionEvent> kafkaTemplate,
            PromotionEventProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void sendEvent(PromotionEvent event) {
        if (event == null || event.campaignId() == null || event.userId() == null || event.eventType() == null) {
            log.warn("Skipping malformed promotion event: {}", event);
            return;
        }

        String key = event.campaignId().toString();
        Message<PromotionEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, properties.getTopic())
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(EVENT_TYPE_HEADER, event.eventType())
                .build();

        try {
            kafkaTemplate.send(message).whenComplete((result, error) -> {
                if (error != null) {
                    log.error(
                            "Failed to publish promotion event type={} campaignId={} userId={}",
                            event.eventType(),
                            event.campaignId(),
                            event.userId(),
                            error
                    );
                    return;
                }

                if (result != null) {
                    log.debug(
                            "Published promotion event type={} campaignId={} partition={} offset={}",
                            event.eventType(),
                            event.campaignId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                }
            });
        } catch (Exception ex) {
            log.error(
                    "Failed to queue promotion event type={} campaignId={} userId={}",
                    event.eventType(),
                    event.campaignId(),
                    event.userId(),
                    ex
            );
        }
    }
}