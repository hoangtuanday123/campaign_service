package com.example.campaignservice.integration.redis;

import com.example.campaignservice.dto.campaign.CampaignResponse;
import com.example.campaignservice.service.CampaignCacheService;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class RedisCampaignCacheService implements CampaignCacheService {

    private static final String ACTIVE_LIST_KEY = "campaign:active:list";
    private static final String ACTIVE_DETAIL_KEY = "campaign:active:%s";
    private static final String QUOTA_KEY = "campaign:quota:%s";

    private static final DefaultRedisScript<Long> DECREMENT_QUOTA_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('GET', KEYS[1]) "
                    + "if not current then return -2 end "
                    + "if tonumber(current) <= 0 then return -1 end "
                    + "return redis.call('DECR', KEYS[1])",
            Long.class
    );

    private final RedisTemplate<String, CampaignResponse> campaignRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisCampaignCacheService(
            RedisTemplate<String, CampaignResponse> campaignRedisTemplate,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.campaignRedisTemplate = campaignRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void cacheActiveCampaign(CampaignResponse campaign) {
        ValueOperations<String, CampaignResponse> detailOps = campaignRedisTemplate.opsForValue();
        ListOperations<String, String> listOps = stringRedisTemplate.opsForList();

        String campaignId = campaign.id().toString();
        detailOps.set(activeDetailKey(campaign.id()), campaign);
        stringRedisTemplate.opsForValue().set(quotaKey(campaign.id()), String.valueOf(campaign.remainingQuota()));
        listOps.remove(ACTIVE_LIST_KEY, 0, campaignId);
        listOps.rightPush(ACTIVE_LIST_KEY, campaignId);
    }

    @Override
    public void evictActiveCampaign(UUID campaignId) {
        stringRedisTemplate.opsForList().remove(ACTIVE_LIST_KEY, 0, campaignId.toString());
        campaignRedisTemplate.delete(activeDetailKey(campaignId));
        stringRedisTemplate.delete(quotaKey(campaignId));
    }

    @Override
    public Optional<CampaignResponse> getActiveCampaign(UUID campaignId) {
        return Optional.ofNullable(campaignRedisTemplate.opsForValue().get(activeDetailKey(campaignId)));
    }

    @Override
    public List<CampaignResponse> getActiveCampaigns() {
        List<String> campaignIds = stringRedisTemplate.opsForList().range(ACTIVE_LIST_KEY, 0, -1);
        if (campaignIds == null || campaignIds.isEmpty()) {
            return Collections.emptyList();
        }

        return campaignIds.stream()
                .map(UUID::fromString)
                .map(this::getActiveCampaign)
                .flatMap(Optional::stream)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public long decrementRemainingQuota(UUID campaignId) {
        Long result = stringRedisTemplate.execute(DECREMENT_QUOTA_SCRIPT, List.of(quotaKey(campaignId)));
        return result == null ? -2L : result;
    }

    private String activeDetailKey(UUID campaignId) {
        return ACTIVE_DETAIL_KEY.formatted(campaignId);
    }

    private String quotaKey(UUID campaignId) {
        return QUOTA_KEY.formatted(campaignId);
    }
}