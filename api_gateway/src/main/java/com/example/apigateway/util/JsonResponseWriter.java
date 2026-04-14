package com.example.apigateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JsonResponseWriter {

    private final ObjectMapper objectMapper;

    public JsonResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerHttpResponse response, HttpStatus status, Object body) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] payload = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(payload);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException ex) {
            byte[] fallbackPayload = "{\"status\":500,\"message\":\"An unexpected error occurred\"}"
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer fallbackBuffer = response.bufferFactory().wrap(fallbackPayload);
            return response.writeWith(Mono.just(fallbackBuffer));
        }
    }
}
