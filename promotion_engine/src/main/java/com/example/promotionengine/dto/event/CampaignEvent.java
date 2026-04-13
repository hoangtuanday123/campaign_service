package com.example.promotionengine.dto.event;

import java.time.Instant;
import java.util.UUID;

public record CampaignEvent(
        UUID eventId,
        String eventType,
        Instant timestamp,
        CampaignEventData data
) {
}