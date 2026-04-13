package com.example.promotionengine.client.user;

import com.example.promotionengine.dto.user.UserSnapshot;
import com.example.promotionengine.exception.DownstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RestUserServiceClient implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RestUserServiceClient.class);

    private final RestClient restClient;
    private final UserServiceProperties properties;

    public RestUserServiceClient(RestClient restClient, UserServiceProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public Optional<UserSnapshot> getUser(UUID userId) {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new DownstreamServiceException("User Service base URL is not configured", null);
        }

        try {
            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(properties.getBaseUrl() + properties.getUserByIdPath(), userId);

            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                request = request.header("X-API-Key", properties.getApiKey());
            }

            UserEnvelope response = request.retrieve().body(UserEnvelope.class);
            if (response == null || response.data() == null) {
                return Optional.empty();
            }

            return Optional.of(response.data());
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }

            log.warn("User Service lookup failed for user {} with status {}", userId, ex.getStatusCode().value());
            throw new DownstreamServiceException("User Service is unavailable or unauthorized", ex);
        } catch (Exception ex) {
            log.warn("User Service lookup failed for user {}", userId, ex);
            throw new DownstreamServiceException("User Service is unavailable or unauthorized", ex);
        }
    }

    private record UserEnvelope(
            Instant timestamp,
            int status,
            String message,
            UserSnapshot data
    ) {
    }
}