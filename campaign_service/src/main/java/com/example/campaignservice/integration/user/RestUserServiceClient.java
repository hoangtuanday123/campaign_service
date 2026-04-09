package com.example.campaignservice.integration.user;

import com.example.campaignservice.service.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
            return Optional.empty();
        }

        try {
            UserEnvelope response = restClient.get()
                    .uri(properties.getBaseUrl() + properties.getUserByIdPath(), userId)
                    .retrieve()
                    .body(UserEnvelope.class);

            if (response == null || response.data() == null) {
                return Optional.empty();
            }

            return Optional.of(new UserSnapshot(
                    response.data().id(),
                    response.data().isNewUser(),
                    response.data().createdAt()
            ));
        } catch (Exception ex) {
            log.warn("User Service lookup failed for user {}", userId, ex);
            return Optional.empty();
        }
    }

    private record UserEnvelope(
            Instant timestamp,
            int status,
            String message,
            UserData data
    ) {
    }

    private record UserData(
            UUID id,
            boolean isNewUser,
            Instant createdAt
    ) {
    }
}