package com.example.promotionengine.service;

import com.example.promotionengine.dto.promotion.ApplyPromotionRequest;
import com.example.promotionengine.dto.promotion.PromotionEligibilityResponse;

public interface PromotionService {

    PromotionEligibilityResponse apply(ApplyPromotionRequest request);
}