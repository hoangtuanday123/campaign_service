package com.example.promotionengine.producer;

import com.example.promotionengine.config.PromotionEventProperties;
import com.example.promotionengine.dto.event.PromotionEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionEventProducerTest {

    @Mock
    private KafkaTemplate<String, PromotionEvent> kafkaTemplate;

    @Test
    void shouldSendEventWithCampaignKey() {
        PromotionEventProperties properties = new PromotionEventProperties();
        PromotionEventProducer producer = new PromotionEventProducer(kafkaTemplate, properties);
        PromotionEvent event = new PromotionEvent(
                "promotion_applied",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-04-12T00:00:00Z")
        );

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

        producer.sendEvent(event);

        ArgumentCaptor<Message<PromotionEvent>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(kafkaTemplate).send(messageCaptor.capture());
        assertEquals("promotion-events", messageCaptor.getValue().getHeaders().get("kafka_topic"));
        assertEquals(event.campaignId().toString(), messageCaptor.getValue().getHeaders().get("kafka_messageKey"));
        assertEquals("promotion_applied", messageCaptor.getValue().getHeaders().get("eventType"));
        assertEquals(event, messageCaptor.getValue().getPayload());
    }

    @Test
    void shouldIgnoreMalformedEvent() {
        PromotionEventProperties properties = new PromotionEventProperties();
        PromotionEventProducer producer = new PromotionEventProducer(kafkaTemplate, properties);

        producer.sendEvent(new PromotionEvent(null, UUID.randomUUID(), UUID.randomUUID(), Instant.now()));

        verify(kafkaTemplate, never()).send(any(Message.class));
    }

    @Test
    void shouldNotThrowWhenKafkaTemplateFailsImmediately() {
        PromotionEventProperties properties = new PromotionEventProperties();
        PromotionEventProducer producer = new PromotionEventProducer(kafkaTemplate, properties);
        PromotionEvent event = new PromotionEvent(
                "campaign_viewed",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-04-12T00:00:00Z")
        );

        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("Kafka unavailable"));

        assertDoesNotThrow(() -> producer.sendEvent(event));
    }
}