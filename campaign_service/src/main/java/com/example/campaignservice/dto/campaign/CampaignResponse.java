package com.example.campaignservice.dto.campaign;

import com.example.campaignservice.domain.entity.Campaign;
import com.example.campaignservice.domain.enums.CampaignStatus;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
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

    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getStatus(),
                campaign.getStartTime(),
                campaign.getEndTime(),
                campaign.getQuota(),
                campaign.getUsedCount(),
                campaign.remainingQuota(),
                campaign.getRuleId(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt()
        );
    }

    public boolean isCurrentlyActive(Instant now) {
        return status == CampaignStatus.ACTIVE
                && !now.isBefore(startTime)
                && !now.isAfter(endTime)
                && remainingQuota > 0;
    }
}