package com.example.promotionengine.service;

import com.example.promotionengine.dto.promotion.ApplyPromotionRequest;
import com.example.promotionengine.dto.promotion.PromotionEligibilityResponse;
import com.example.promotionengine.dto.promotion.PromotionInteractionRequest;

public interface PromotionService {

    PromotionEligibilityResponse apply(ApplyPromotionRequest request);

    void trackCampaignView(PromotionInteractionRequest request);

    void trackCampaignClick(PromotionInteractionRequest request);
}