package com.example.promotionengine.dto.promotion;

public record PromotionEligibilityResponse(
        boolean eligible,
        String message
) {

    public static PromotionEligibilityResponse eligible(String message) {
        return new PromotionEligibilityResponse(true, message);
    }

    public static PromotionEligibilityResponse ineligible(String message) {
        return new PromotionEligibilityResponse(false, message);
    }
}