package com.example.apigateway.util;

import com.example.apigateway.config.GatewayPublicPathsProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class PathMatcherUtil {

    private final GatewayPublicPathsProperties gatewayPublicPathsProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public PathMatcherUtil(GatewayPublicPathsProperties gatewayPublicPathsProperties) {
        this.gatewayPublicPathsProperties = gatewayPublicPathsProperties;
    }

    public boolean isPublic(String path) {
        return gatewayPublicPathsProperties.publicPaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }
}
