package com.example.analyticsservice.controller;

import com.example.analyticsservice.common.api.ApiResponse;
import com.example.analyticsservice.dto.analytics.CampaignAnalyticsResponse;
import com.example.analyticsservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/analytics", "/api/analytics"})
@Tag(name = "Analytics", description = "Campaign analytics reporting endpoints")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/campaign/{campaignId}")
    @Operation(summary = "Get aggregated analytics for a campaign")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKeyAuth")
    public ResponseEntity<ApiResponse<CampaignAnalyticsResponse>> getCampaignAnalytics(@PathVariable UUID campaignId) {
        CampaignAnalyticsResponse response = analyticsService.getCampaignAnalytics(campaignId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Campaign analytics retrieved successfully", response));
    }

    @GetMapping("/campaign/{campaignId}/report")
    @Operation(summary = "Get the final analytics report for a completed campaign")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "apiKeyAuth")
    public ResponseEntity<ApiResponse<CampaignAnalyticsResponse>> getCampaignReport(@PathVariable UUID campaignId) {
        CampaignAnalyticsResponse response = analyticsService.getCampaignReport(campaignId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "Campaign analytics report retrieved successfully", response));
    }
}