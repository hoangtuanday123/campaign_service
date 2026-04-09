package com.example.campaignservice.service;

import java.util.UUID;

public interface CampaignEventPublisher {

    void publishCreated(UUID campaignId);

    void publishActivated(UUID campaignId);

    void publishDeactivated(UUID campaignId);
}