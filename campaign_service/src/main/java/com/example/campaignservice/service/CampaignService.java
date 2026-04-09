package com.example.campaignservice.service;

import com.example.campaignservice.dto.campaign.CampaignResponse;
import com.example.campaignservice.dto.campaign.CreateCampaignRequest;
import com.example.campaignservice.dto.campaign.UpdateCampaignRequest;

import java.util.List;
import java.util.UUID;

public interface CampaignService {

    CampaignResponse create(CreateCampaignRequest request);

    CampaignResponse getById(UUID id);

    CampaignResponse update(UUID id, UpdateCampaignRequest request);

    CampaignResponse activate(UUID id);

    CampaignResponse deactivate(UUID id);

    List<CampaignResponse> getActiveCampaigns();
}