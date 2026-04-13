package com.example.promotionengine.service.impl;

import com.example.promotionengine.dto.campaign.CampaignDetails;
import com.example.promotionengine.service.CampaignCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedisCampaignCacheService implements CampaignCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCampaignCacheService.class);
    private static final String CAMPAIGN_KEY = "promotion:campaign:%s";
    private static final String CAMPAIGN_QUOTA_KEY = "promotion:campaign:quota:%s";

    private final RedisTemplate<String, CampaignDetails> campaignRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final Clock clock;

    public RedisCampaignCacheService(
            RedisTemplate<String, CampaignDetails> campaignRedisTemplate,
            StringRedisTemplate stringRedisTemplate,
            Clock clock
    ) {
        this.campaignRedisTemplate = campaignRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.clock = clock;
    }

    @Override
    public Optional<CampaignDetails> getCampaign(UUID campaignId) {
        try {
            return Optional.ofNullable(campaignRedisTemplate.opsForValue().get(campaignKey(campaignId)));
        } catch (Exception ex) {
            log.warn("Redis campaign cache lookup failed for campaign {}", campaignId, ex);
            return Optional.empty();
        }
    }

    @Override
    public void cacheCampaign(CampaignDetails campaign) {
        try {
            Duration ttl = cacheTtl(campaign.endTime());
            campaignRedisTemplate.opsForValue().set(campaignKey(campaign.id()), campaign, ttl);
            stringRedisTemplate.opsForValue().set(campaignQuotaKey(campaign.id()), String.valueOf(campaign.remainingQuota()), ttl);
        } catch (Exception ex) {
            log.warn("Redis campaign cache update failed for campaign {}", campaign.id(), ex);
        }
    }

    @Override
    public void evictCampaign(UUID campaignId) {
        try {
            campaignRedisTemplate.delete(campaignKey(campaignId));
            stringRedisTemplate.delete(campaignQuotaKey(campaignId));
        } catch (Exception ex) {
            log.warn("Redis campaign cache eviction failed for campaign {}", campaignId, ex);
        }
    }

    private Duration cacheTtl(Instant endTime) {
        Duration ttl = Duration.between(Instant.now(clock), endTime);
        if (ttl.isNegative() || ttl.isZero()) {
            return Duration.ofSeconds(1);
        }
        return ttl;
    }

    private String campaignKey(UUID campaignId) {
        return CAMPAIGN_KEY.formatted(campaignId);
    }

    private String campaignQuotaKey(UUID campaignId) {
        return CAMPAIGN_QUOTA_KEY.formatted(campaignId);
    }
}