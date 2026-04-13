package com.example.promotionengine.client.campaign;

import com.example.promotionengine.dto.campaign.CampaignDetails;

import java.util.Optional;
import java.util.UUID;

public interface CampaignServiceClient {

    Optional<CampaignDetails> getCampaign(UUID campaignId);
}