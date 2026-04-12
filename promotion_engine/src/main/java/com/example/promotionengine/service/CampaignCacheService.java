package com.example.promotionengine.service;

import com.example.promotionengine.dto.campaign.CampaignDetails;

import java.util.Optional;
import java.util.UUID;

public interface CampaignCacheService {

    Optional<CampaignDetails> getCampaign(UUID campaignId);

    void cacheCampaign(CampaignDetails campaign);

    void evictCampaign(UUID campaignId);
}