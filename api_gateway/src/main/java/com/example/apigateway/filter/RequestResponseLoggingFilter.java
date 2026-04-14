package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        log.info(
                "Gateway request: method={}, path={}, headers={}",
                request.getMethod(),
                request.getURI().getRawPath(),
                sanitizeHeaders(request)
        );

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long latency = System.currentTimeMillis() - startTime;
                    Integer status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : null;

                    log.info(
                            "Gateway response: method={}, path={}, status={}, latencyMs={}",
                            request.getMethod(),
                            request.getURI().getRawPath(),
                            status,
                            latency
                    );
                });
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private Map<String, List<String>> sanitizeHeaders(ServerHttpRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if ("authorization".equalsIgnoreCase(name)) {
                headers.put(name, List.of("Bearer ***"));
                return;
            }

            headers.put(name, values);
        });
        return headers;
    }
}
