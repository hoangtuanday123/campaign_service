package com.example.promotionengine.dto.promotion;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PromotionInteractionRequest(
        @NotNull(message = "userId is required")
        UUID userId,

        @NotNull(message = "campaignId is required")
        UUID campaignId
) {
}