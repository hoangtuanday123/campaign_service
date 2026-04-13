package com.example.analyticsservice.consumer;

import com.example.analyticsservice.dto.event.CampaignEvent;
import com.example.analyticsservice.dto.event.CampaignEventData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CampaignEventConsumerTest {

    @Mock
    private CampaignActivatedEventHandler campaignActivatedEventHandler;

    @Mock
    private CampaignDeactivatedEventHandler campaignDeactivatedEventHandler;

    @Test
    void shouldDelegateActivatedEventsToMatchingHandler() {
        CampaignEventConsumer consumer = new CampaignEventConsumer(List.of(
                handler(campaignActivatedEventHandler, AnalyticsEventType.CAMPAIGN_ACTIVATED.value()),
                handler(campaignDeactivatedEventHandler, AnalyticsEventType.CAMPAIGN_DEACTIVATED.value())
        ));
        CampaignEvent event = event(AnalyticsEventType.CAMPAIGN_ACTIVATED.value());

        consumer.consume(event);

        verify(campaignActivatedEventHandler).handle(event);
        verify(campaignDeactivatedEventHandler, never()).handle(event);
    }

    @Test
    void shouldIgnoreUnsupportedEventTypes() {
        CampaignEventConsumer consumer = new CampaignEventConsumer(List.of(
                handler(campaignActivatedEventHandler, AnalyticsEventType.CAMPAIGN_ACTIVATED.value())
        ));

        consumer.consume(event("campaign_created"));

        verify(campaignActivatedEventHandler, never()).handle(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldIgnoreMalformedEvents() {
        CampaignEventConsumer consumer = new CampaignEventConsumer(List.of(
                handler(campaignActivatedEventHandler, AnalyticsEventType.CAMPAIGN_ACTIVATED.value())
        ));

        consumer.consume(new CampaignEvent(null, AnalyticsEventType.CAMPAIGN_ACTIVATED.value(), Instant.now(), null));

        verify(campaignActivatedEventHandler, never()).handle(org.mockito.ArgumentMatchers.any());
    }

    private AnalyticsEventHandler handler(AnalyticsEventHandler delegate, String eventType) {
        return new AnalyticsEventHandler() {
            @Override
            public String supportsEventType() {
                return eventType;
            }

            @Override
            public void handle(CampaignEvent event) {
                delegate.handle(event);
            }
        };
    }

    private CampaignEvent event(String eventType) {
        return new CampaignEvent(
                UUID.randomUUID(),
                eventType,
                Instant.parse("2026-04-13T08:30:00Z"),
                new CampaignEventData(UUID.randomUUID())
        );
    }
}