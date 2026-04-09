package com.example.campaignservice.service.impl;

import com.example.campaignservice.domain.entity.Campaign;
import com.example.campaignservice.domain.enums.CampaignStatus;
import com.example.campaignservice.dto.campaign.CreateCampaignRequest;
import com.example.campaignservice.exception.CampaignValidationException;
import com.example.campaignservice.exception.InvalidCampaignStateException;
import com.example.campaignservice.repository.CampaignRepository;
import com.example.campaignservice.service.CampaignCacheService;
import com.example.campaignservice.service.CampaignEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignCacheService campaignCacheService;

    @Mock
    private CampaignEventPublisher campaignEventPublisher;

    private CampaignServiceImpl campaignService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-09T10:15:30Z"), ZoneOffset.UTC);
        campaignService = new CampaignServiceImpl(campaignRepository, campaignCacheService, campaignEventPublisher, fixedClock);
    }

    @Test
    void createShouldPersistDraftCampaign() {
        CreateCampaignRequest request = new CreateCampaignRequest(
                "Spring Sale",
                Instant.parse("2026-04-10T00:00:00Z"),
                Instant.parse("2026-04-15T00:00:00Z"),
                500,
                "rule-123"
        );

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(invocation -> {
            Campaign campaign = invocation.getArgument(0);
            campaign.setCreatedAt(Instant.parse("2026-04-09T10:15:30Z"));
            campaign.setUpdatedAt(Instant.parse("2026-04-09T10:15:30Z"));
            return campaign;
        });

        var response = campaignService.create(request);

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        Campaign saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        assertThat(saved.getUsedCount()).isZero();
        assertThat(response.status()).isEqualTo(CampaignStatus.DRAFT);
        verify(campaignEventPublisher).publishCreated(response.id());
    }

    @Test
    void activateShouldRejectAlreadyActiveCampaign() {
        UUID id = UUID.randomUUID();
        Campaign campaign = campaign(id, CampaignStatus.ACTIVE, 100, 0,
                Instant.parse("2026-04-09T00:00:00Z"), Instant.parse("2026-04-10T00:00:00Z"));

        when(campaignRepository.findByIdForUpdate(id)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> campaignService.activate(id))
                .isInstanceOf(InvalidCampaignStateException.class)
                .hasMessageContaining("already active");

        verify(campaignRepository, never()).save(any(Campaign.class));
        verify(campaignCacheService, never()).cacheActiveCampaign(any());
    }

    @Test
    void activateShouldRejectExpiredCampaign() {
        UUID id = UUID.randomUUID();
        Campaign campaign = campaign(id, CampaignStatus.DRAFT, 100, 10,
                Instant.parse("2026-04-01T00:00:00Z"), Instant.parse("2026-04-05T00:00:00Z"));

        when(campaignRepository.findByIdForUpdate(id)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> campaignService.activate(id))
                .isInstanceOf(CampaignValidationException.class)
                .hasMessageContaining("outside its active schedule");
    }

    private Campaign campaign(UUID id, CampaignStatus status, int quota, int usedCount, Instant startTime, Instant endTime) {
        return Campaign.builder()
                .id(id)
                .name("Campaign")
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .quota(quota)
                .usedCount(usedCount)
                .ruleId("rule-123")
                .createdAt(Instant.parse("2026-04-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-04-01T00:00:00Z"))
                .build();
    }
}