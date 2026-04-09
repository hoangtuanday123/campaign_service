package com.example.campaignservice.service;

import com.example.campaignservice.dto.campaign.CampaignResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignCacheService {

    void cacheActiveCampaign(CampaignResponse campaign);

    void evictActiveCampaign(UUID campaignId);

    Optional<CampaignResponse> getActiveCampaign(UUID campaignId);

    List<CampaignResponse> getActiveCampaigns();

    long decrementRemainingQuota(UUID campaignId);
}