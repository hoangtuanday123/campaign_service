package com.example.promotionengine.dto.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Uses UUID identifiers to stay aligned with the current service contracts.
 */
public record PromotionEvent(
        String eventType,
        UUID userId,
        UUID campaignId,
        Instant timestamp
) {
}