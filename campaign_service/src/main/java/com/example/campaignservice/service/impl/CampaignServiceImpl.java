package com.example.campaignservice.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.campaignservice.domain.entity.Campaign;
import com.example.campaignservice.domain.enums.CampaignStatus;
import com.example.campaignservice.dto.campaign.CampaignResponse;
import com.example.campaignservice.dto.campaign.CreateCampaignRequest;
import com.example.campaignservice.dto.campaign.UpdateCampaignRequest;
import com.example.campaignservice.exception.CampaignValidationException;
import com.example.campaignservice.exception.InvalidCampaignStateException;
import com.example.campaignservice.exception.ResourceNotFoundException;
import com.example.campaignservice.repository.CampaignRepository;
import com.example.campaignservice.service.CampaignCacheService;
import com.example.campaignservice.service.CampaignEventPublisher;
import com.example.campaignservice.service.CampaignService;

@Service
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignCacheService campaignCacheService;
    private final CampaignEventPublisher campaignEventPublisher;
    private final Clock clock;

    public CampaignServiceImpl(
            CampaignRepository campaignRepository,
            CampaignCacheService campaignCacheService,
            CampaignEventPublisher campaignEventPublisher,
            Clock clock
    ) {
        this.campaignRepository = campaignRepository;
        this.campaignCacheService = campaignCacheService;
        this.campaignEventPublisher = campaignEventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CampaignResponse create(CreateCampaignRequest request) {
        Campaign campaign = Campaign.builder()
                .id(UUID.randomUUID())
                .name(request.name().trim())
                .status(CampaignStatus.DRAFT)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .quota(request.quota())
                .usedCount(0)
                .ruleId(request.ruleId().trim())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        CampaignResponse response = CampaignResponse.from(saved);
        afterCommit(() -> campaignEventPublisher.publishCreated(saved.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CampaignResponse getById(UUID id) {
        return CampaignResponse.from(getCampaign(id));
    }

    @Override
    @Transactional
    public CampaignResponse update(UUID id, UpdateCampaignRequest request) {
        Campaign campaign = campaignRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));

        if (campaign.getStatus() == CampaignStatus.ACTIVE) {
            throw new InvalidCampaignStateException("Active campaigns must be deactivated before update");
        }

        campaign.setName(request.name().trim());
        campaign.setStartTime(request.startTime());
        campaign.setEndTime(request.endTime());
        campaign.setQuota(request.quota());
        campaign.setRuleId(request.ruleId().trim());

        Campaign saved = campaignRepository.save(campaign);
        return CampaignResponse.from(saved);
    }

    @Override
    @Transactional
    public CampaignResponse activate(UUID id) {
        Campaign campaign = campaignRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));

        if (campaign.getStatus() == CampaignStatus.ACTIVE) {
            throw new InvalidCampaignStateException("Campaign is already active");
        }

        Instant now = Instant.now(clock);
        if (!campaign.isWithinSchedule(now)) {
            throw new CampaignValidationException("Campaign cannot be activated outside its active schedule");
        }
        if (campaign.remainingQuota() <= 0) {
            throw new CampaignValidationException("Campaign cannot be activated because quota is exhausted");
        }

        campaign.setStatus(CampaignStatus.ACTIVE);
        Campaign saved = campaignRepository.save(campaign);
        CampaignResponse response = CampaignResponse.from(saved);

        afterCommit(() -> {
            campaignCacheService.cacheActiveCampaign(response);
            campaignEventPublisher.publishActivated(saved.getId());
        });
        return response;
    }

    @Override
    @Transactional
    public CampaignResponse deactivate(UUID id) {
        Campaign campaign = campaignRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));

        if (campaign.getStatus() == CampaignStatus.INACTIVE) {
            CampaignResponse response = CampaignResponse.from(campaign);
            afterCommit(() -> campaignCacheService.evictActiveCampaign(campaign.getId()));
            return response;
        }

        campaign.setStatus(CampaignStatus.INACTIVE);
        Campaign saved = campaignRepository.save(campaign);
        CampaignResponse response = CampaignResponse.from(saved);

        afterCommit(() -> {
            campaignCacheService.evictActiveCampaign(saved.getId());
            campaignEventPublisher.publishDeactivated(saved.getId());
        });
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        Instant now = Instant.now(clock);
        List<CampaignResponse> cachedCampaigns = campaignCacheService.getActiveCampaigns().stream()
                .filter(campaign -> campaign.isCurrentlyActive(now))
                .sorted(Comparator.comparing(CampaignResponse::startTime))
                .toList();

        if (!cachedCampaigns.isEmpty()) {
            return cachedCampaigns;
        }

        List<CampaignResponse> activeCampaigns = campaignRepository
                .findEligibleActiveCampaigns(CampaignStatus.ACTIVE, now)
                .stream()
                .map(CampaignResponse::from)
                .toList();

        activeCampaigns.forEach(campaignCacheService::cacheActiveCampaign);
        return activeCampaigns;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampaignResponse> getAll() {
        return campaignRepository.findAll().stream()
                .map(CampaignResponse::from)
                .toList();
    }

    private Campaign getCampaign(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    private void afterCommit(Runnable runnable) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runnable.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }
}