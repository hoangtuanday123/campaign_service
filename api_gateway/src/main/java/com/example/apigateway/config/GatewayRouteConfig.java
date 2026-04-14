package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder, ServiceRoutesProperties serviceRoutesProperties) {
        return builder.routes()
                .route("user-service", route -> route
                        .path("/api/users", "/api/users/**")
                        .filters(filters -> filters
                                .rewritePath("/api/users(?<segment>/?.*)", "/api/v1/users${segment}")
                                .preserveHostHeader()
                        )
                        .uri(serviceRoutesProperties.user().baseUrl())
                )
                .route("campaign-service", route -> route
                        .path("/api/campaigns", "/api/campaigns/**")
                        .filters(filters -> filters
                                .rewritePath("/api/campaigns(?<segment>/?.*)", "/api/v1/campaigns${segment}")
                                .preserveHostHeader()
                        )
                        .uri(serviceRoutesProperties.campaign().baseUrl())
                )
                .route("promotion-engine", route -> route
                        .path("/api/promotions", "/api/promotions/**")
                        .filters(filters -> filters
                                .rewritePath("/api/promotions(?<segment>/?.*)", "/api/v1/promotions${segment}")
                                .preserveHostHeader()
                        )
                        .uri(serviceRoutesProperties.promotion().baseUrl())
                )
                .route("analytics-service", route -> route
                        .path("/api/analytics", "/api/analytics/**")
                        .filters(filters -> filters
                                .rewritePath("/api/analytics(?<segment>/?.*)", "/api/v1/analytics${segment}")
                                .preserveHostHeader()
                        )
                        .uri(serviceRoutesProperties.analytics().baseUrl())
                )
                .build();
    }
}
