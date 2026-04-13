package com.example.promotionengine.dto.campaign;

import java.time.Instant;
import java.util.UUID;

public record CampaignDetails(
        UUID id,
        String name,
        CampaignStatus status,
        Instant startTime,
        Instant endTime,
        int quota,
        int usedCount,
        int remainingQuota,
        String ruleId,
        Instant createdAt,
        Instant updatedAt
) {

    public boolean isCurrentlyActive(Instant now) {
        return status == CampaignStatus.ACTIVE
                && !now.isBefore(startTime)
                && !now.isAfter(endTime)
                && remainingQuota > 0;
    }
}