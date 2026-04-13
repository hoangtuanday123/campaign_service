package com.example.campaignservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.campaignservice.common.api.ApiResponse;
import com.example.campaignservice.dto.campaign.CampaignResponse;
import com.example.campaignservice.dto.campaign.CreateCampaignRequest;
import com.example.campaignservice.dto.campaign.UpdateCampaignRequest;
import com.example.campaignservice.service.CampaignService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/campaigns")
@Tag(name = "Campaigns", description = "Campaign lifecycle management endpoints")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @Operation(summary = "Create a campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> create(@Valid @RequestBody CreateCampaignRequest request) {
        CampaignResponse response = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Campaign created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a campaign by id")
    public ResponseEntity<ApiResponse<CampaignResponse>> getById(@PathVariable UUID id) {
        CampaignResponse response = campaignService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Campaign retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCampaignRequest request
    ) {
        CampaignResponse response = campaignService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Campaign updated successfully", response));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate a campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> activate(@PathVariable UUID id) {
        CampaignResponse response = campaignService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Campaign activated successfully", response));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> deactivate(@PathVariable UUID id) {
        CampaignResponse response = campaignService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Campaign deactivated successfully", response));
    }

    @GetMapping("/active")
    @Operation(summary = "List currently active campaigns")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaigns() {
        List<CampaignResponse> response = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Active campaigns retrieved successfully", response));
    }
}