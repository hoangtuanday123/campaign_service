package com.example.promotionengine.service.impl;

import com.example.promotionengine.service.PromotionUsageClaimResult;
import com.example.promotionengine.service.PromotionUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RedisPromotionUsageService implements PromotionUsageService {

    private static final Logger log = LoggerFactory.getLogger(RedisPromotionUsageService.class);
    private static final String CAMPAIGN_QUOTA_KEY = "promotion:campaign:quota:%s";

    // One Redis script claims the user usage key and decrements cached quota in a single round-trip.
    private static final DefaultRedisScript<Long> CLAIM_USAGE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 1 then return -1 end "
                    + "local quota = redis.call('GET', KEYS[2]) "
                    + "if not quota then return -2 end "
                    + "if tonumber(quota) <= 0 then return -3 end "
                    + "redis.call('SET', KEYS[1], '1', 'EX', ARGV[1]) "
                    + "return redis.call('DECR', KEYS[2])",
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final Clock clock;

    public RedisPromotionUsageService(StringRedisTemplate stringRedisTemplate, Clock clock) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.clock = clock;
    }

    @Override
    public PromotionUsageClaimResult claimUsage(UUID campaignId, UUID userId, Instant expiresAt) {
        try {
            Duration ttl = Duration.between(Instant.now(clock), expiresAt);
            long ttlSeconds = Math.max(1L, ttl.getSeconds());

            Long result = stringRedisTemplate.execute(
                    CLAIM_USAGE_SCRIPT,
                    List.of(usageKey(campaignId, userId), campaignQuotaKey(campaignId)),
                    String.valueOf(ttlSeconds)
            );

            if (result == null || result == -2L) {
                return PromotionUsageClaimResult.CACHE_UNAVAILABLE;
            }
            if (result == -1L) {
                return PromotionUsageClaimResult.ALREADY_USED;
            }
            if (result == -3L) {
                return PromotionUsageClaimResult.QUOTA_EXHAUSTED;
            }
            return PromotionUsageClaimResult.CLAIMED;
        } catch (Exception ex) {
            log.warn("Redis promotion usage claim failed for campaign {} and user {}", campaignId, userId, ex);
            return PromotionUsageClaimResult.CACHE_UNAVAILABLE;
        }
    }

    private String usageKey(UUID campaignId, UUID userId) {
        return campaignId + ":" + userId;
    }

    private String campaignQuotaKey(UUID campaignId) {
        return CAMPAIGN_QUOTA_KEY.formatted(campaignId);
    }
}