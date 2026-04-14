package com.example.apigateway.exception;

import com.example.apigateway.common.api.ApiResponse;
import com.example.apigateway.util.JsonResponseWriter;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final JsonResponseWriter jsonResponseWriter;

    public GlobalExceptionHandler(JsonResponseWriter jsonResponseWriter) {
        this.jsonResponseWriter = jsonResponseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof InvalidJwtException invalidJwtException) {
            return jsonResponseWriter.write(response, HttpStatus.UNAUTHORIZED,
                    ApiResponse.success(HttpStatus.UNAUTHORIZED.value(), invalidJwtException.getMessage()));
        }

        if (ex instanceof RateLimitExceededException rateLimitExceededException) {
            return jsonResponseWriter.write(response, HttpStatus.TOO_MANY_REQUESTS,
                    ApiResponse.success(HttpStatus.TOO_MANY_REQUESTS.value(), rateLimitExceededException.getMessage()));
        }

        if (ex instanceof RedisConnectionFailureException) {
            log.warn("Redis connection failure detected", ex);
            return jsonResponseWriter.write(response, HttpStatus.SERVICE_UNAVAILABLE,
                    ApiResponse.success(HttpStatus.SERVICE_UNAVAILABLE.value(), "Rate limiting service unavailable"));
        }

        if (isTimeout(ex)) {
            log.warn("Downstream timeout detected", ex);
            return jsonResponseWriter.write(response, HttpStatus.GATEWAY_TIMEOUT,
                    ApiResponse.success(HttpStatus.GATEWAY_TIMEOUT.value(), "Downstream service timeout"));
        }

        if (isUnavailable(ex)) {
            log.warn("Downstream service unavailable", ex);
            return jsonResponseWriter.write(response, HttpStatus.SERVICE_UNAVAILABLE,
                    ApiResponse.success(HttpStatus.SERVICE_UNAVAILABLE.value(), "Downstream service unavailable"));
        }

        if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatus status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            String message = responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : "Request failed";
            return jsonResponseWriter.write(response, status, ApiResponse.success(status.value(), message));
        }

        log.error("Unhandled exception", ex);
        return jsonResponseWriter.write(response, HttpStatus.INTERNAL_SERVER_ERROR,
                ApiResponse.success(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
    }

    @Override
    public int getOrder() {
        return -2;
    }

    private boolean isTimeout(Throwable throwable) {
        return throwable instanceof TimeoutException
                || throwable instanceof ReadTimeoutException
                || throwable instanceof ConnectTimeoutException
                || throwable.getCause() instanceof TimeoutException
                || throwable.getCause() instanceof ReadTimeoutException
                || throwable.getCause() instanceof ConnectTimeoutException;
    }

    private boolean isUnavailable(Throwable throwable) {
        return throwable instanceof ConnectException
                || throwable.getCause() instanceof ConnectException;
    }
}
