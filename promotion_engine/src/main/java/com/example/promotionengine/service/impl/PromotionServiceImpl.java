package com.example.promotionengine.service.impl;

import com.example.promotionengine.client.campaign.CampaignServiceClient;
import com.example.promotionengine.client.user.UserServiceClient;
import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.dto.campaign.CampaignStatus;
import com.example.promotionengine.dto.promotion.ApplyPromotionRequest;
import com.example.promotionengine.dto.promotion.PromotionEligibilityResponse;
import com.example.promotionengine.dto.user.UserSnapshot;
import com.example.promotionengine.service.CampaignCacheService;
import com.example.promotionengine.service.PromotionService;
import com.example.promotionengine.service.PromotionUsageClaimResult;
import com.example.promotionengine.service.PromotionUsageService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final UserServiceClient userServiceClient;
    private final CampaignServiceClient campaignServiceClient;
    private final CampaignCacheService campaignCacheService;
    private final PromotionUsageService promotionUsageService;
    private final Clock clock;

    public PromotionServiceImpl(
            UserServiceClient userServiceClient,
            CampaignServiceClient campaignServiceClient,
            CampaignCacheService campaignCacheService,
            PromotionUsageService promotionUsageService,
            Clock clock
    ) {
        this.userServiceClient = userServiceClient;
        this.campaignServiceClient = campaignServiceClient;
        this.campaignCacheService = campaignCacheService;
        this.promotionUsageService = promotionUsageService;
        this.clock = clock;
    }

    @Override
    public PromotionEligibilityResponse apply(ApplyPromotionRequest request) {
        Optional<UserSnapshot> user = userServiceClient.getUser(request.userId());
        if (user.isEmpty()) {
            return PromotionEligibilityResponse.ineligible("User not found");
        }
        if (!user.get().isActive()) {
            return PromotionEligibilityResponse.ineligible("User is inactive");
        }

        CampaignDetails campaign = loadCampaign(request);
        if (campaign == null) {
            return PromotionEligibilityResponse.ineligible("Campaign not found");
        }

        Instant now = Instant.now(clock);
        if (campaign.status() != CampaignStatus.ACTIVE) {
            return PromotionEligibilityResponse.ineligible("Campaign is not active");
        }
        if (now.isBefore(campaign.startTime())) {
            return PromotionEligibilityResponse.ineligible("Campaign is not active yet");
        }
        if (now.isAfter(campaign.endTime())) {
            return PromotionEligibilityResponse.ineligible("Campaign has expired");
        }
        if (campaign.remainingQuota() <= 0) {
            return PromotionEligibilityResponse.ineligible("Campaign quota is exhausted");
        }

        PromotionUsageClaimResult claimResult = claimUsage(campaign, request);
        return switch (claimResult) {
            case CLAIMED -> PromotionEligibilityResponse.eligible("Promotion applied successfully");
            case ALREADY_USED -> PromotionEligibilityResponse.ineligible("User already used promotion");
            case QUOTA_EXHAUSTED -> PromotionEligibilityResponse.ineligible("Campaign quota is exhausted");
            case CACHE_UNAVAILABLE -> PromotionEligibilityResponse.ineligible("Promotion temporarily unavailable");
        };
    }

    private CampaignDetails loadCampaign(ApplyPromotionRequest request) {
        return campaignCacheService.getCampaign(request.campaignId())
                .or(() -> campaignServiceClient.getCampaign(request.campaignId())
                        .map(campaign -> {
                            campaignCacheService.cacheCampaign(campaign);
                            return campaign;
                        }))
                .orElse(null);
    }

    private PromotionUsageClaimResult claimUsage(CampaignDetails campaign, ApplyPromotionRequest request) {
        PromotionUsageClaimResult result = promotionUsageService.claimUsage(
                campaign.id(),
                request.userId(),
                campaign.endTime()
        );

        if (result != PromotionUsageClaimResult.CACHE_UNAVAILABLE) {
            return result;
        }

        Optional<CampaignDetails> refreshedCampaign = campaignServiceClient.getCampaign(campaign.id());
        if (refreshedCampaign.isEmpty()) {
            return PromotionUsageClaimResult.CACHE_UNAVAILABLE;
        }

        campaignCacheService.cacheCampaign(refreshedCampaign.get());
        return promotionUsageService.claimUsage(
                refreshedCampaign.get().id(),
                request.userId(),
                refreshedCampaign.get().endTime()
        );
    }
}