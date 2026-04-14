package com.example.apigateway.filter;

import com.example.apigateway.config.RateLimitProperties;
import com.example.apigateway.exception.RateLimitExceededException;
import com.example.apigateway.util.ClientIdentifierResolver;
import com.example.apigateway.util.ExchangeAttributes;
import com.example.apigateway.util.PathMatcherUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitProperties rateLimitProperties;
    private final PathMatcherUtil pathMatcherUtil;

    public RateLimitFilter(
            ReactiveStringRedisTemplate redisTemplate,
            RateLimitProperties rateLimitProperties,
            PathMatcherUtil pathMatcherUtil
    ) {
        this.redisTemplate = redisTemplate;
        this.rateLimitProperties = rateLimitProperties;
        this.pathMatcherUtil = pathMatcherUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (pathMatcherUtil.isPublic(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        String key = buildKey(exchange);
        Duration window = Duration.ofSeconds(rateLimitProperties.windowSeconds());

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(currentCount -> {
                    Mono<Boolean> expireMono = currentCount == 1
                            ? redisTemplate.expire(key, window)
                            : Mono.just(Boolean.TRUE);

                    return expireMono.flatMap(ignored -> {
                        if (currentCount > rateLimitProperties.maxRequests()) {
                            return Mono.error(new RateLimitExceededException("Rate limit exceeded"));
                        }

                        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(rateLimitProperties.maxRequests()));
                        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                                String.valueOf(Math.max(rateLimitProperties.maxRequests() - currentCount, 0)));
                        return chain.filter(exchange);
                    });
                });
    }

    @Override
    public int getOrder() {
        return -140;
    }

    private String buildKey(ServerWebExchange exchange) {
        String userId = exchange.getAttribute(ExchangeAttributes.AUTHENTICATED_USER_ID);
        ServerHttpRequest request = exchange.getRequest();

        if (userId != null && !userId.isBlank()) {
            return rateLimitProperties.keyPrefix() + ":user:" + userId;
        }

        return rateLimitProperties.keyPrefix() + ":ip:" + ClientIdentifierResolver.resolveClientIp(request);
    }
}
