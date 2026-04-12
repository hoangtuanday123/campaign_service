package com.example.promotionengine.service;

import java.time.Instant;
import java.util.UUID;

public interface PromotionUsageService {

    PromotionUsageClaimResult claimUsage(UUID campaignId, UUID userId, Instant expiresAt);
}