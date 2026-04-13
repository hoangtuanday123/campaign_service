package com.example.promotionengine.controller;

import com.example.promotionengine.common.api.ApiResponse;
import com.example.promotionengine.dto.promotion.ApplyPromotionRequest;
import com.example.promotionengine.dto.promotion.PromotionEligibilityResponse;
import com.example.promotionengine.dto.promotion.PromotionInteractionRequest;
import com.example.promotionengine.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/promotions", "/api/promotions"})
@Tag(name = "Promotions", description = "Promotion eligibility endpoints")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping("/apply")
    @Operation(summary = "Evaluate and apply a promotion for a user")
    public ResponseEntity<ApiResponse<PromotionEligibilityResponse>> apply(
            @Valid @RequestBody ApplyPromotionRequest request
    ) {
        PromotionEligibilityResponse response = promotionService.apply(request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK.value(), "Promotion evaluated successfully", response)
        );
    }

    @PostMapping("/view")
    @Operation(summary = "Track a campaign view event")
    public ResponseEntity<ApiResponse<Void>> view(@Valid @RequestBody PromotionInteractionRequest request) {
        promotionService.trackCampaignView(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(HttpStatus.ACCEPTED.value(), "Campaign view tracked"));
    }

    @PostMapping("/click")
    @Operation(summary = "Track a campaign click event")
    public ResponseEntity<ApiResponse<Void>> click(@Valid @RequestBody PromotionInteractionRequest request) {
        promotionService.trackCampaignClick(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(HttpStatus.ACCEPTED.value(), "Campaign click tracked"));
    }
}