package com.example.apigateway.filter;

import com.example.apigateway.config.GatewayAuthProperties;
import com.example.apigateway.service.JwtService;
import com.example.apigateway.util.ClientIdentifierResolver;
import com.example.apigateway.util.ExchangeAttributes;
import com.example.apigateway.util.PathMatcherUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private final PathMatcherUtil pathMatcherUtil;
    private final GatewayAuthProperties gatewayAuthProperties;

    public AuthenticationFilter(
            JwtService jwtService,
            PathMatcherUtil pathMatcherUtil,
            GatewayAuthProperties gatewayAuthProperties
    ) {
        this.jwtService = jwtService;
        this.pathMatcherUtil = pathMatcherUtil;
        this.gatewayAuthProperties = gatewayAuthProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (pathMatcherUtil.isPublic(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = ClientIdentifierResolver.extractBearerToken(authHeader);
        JwtService.AuthenticatedUser authenticatedUser = jwtService.validate(token);

        exchange.getAttributes().put(ExchangeAttributes.AUTHENTICATED_USER_ID, authenticatedUser.userId().toString());
        exchange.getAttributes().put(ExchangeAttributes.AUTHENTICATED_USERNAME, authenticatedUser.username());

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.remove(gatewayAuthProperties.forwardedUserIdHeader());
                    headers.remove(gatewayAuthProperties.forwardedUsernameHeader());
                    headers.remove("X-Original-Path");
                    headers.add(gatewayAuthProperties.forwardedUserIdHeader(), authenticatedUser.userId().toString());
                    headers.add(gatewayAuthProperties.forwardedUsernameHeader(), authenticatedUser.username());
                    headers.add("X-Original-Path", exchange.getRequest().getURI().getRawPath());
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -150;
    }
}
