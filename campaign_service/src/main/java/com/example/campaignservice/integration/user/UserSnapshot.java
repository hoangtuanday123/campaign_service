package com.example.campaignservice.integration.user;

import java.time.Instant;
import java.util.UUID;

public record UserSnapshot(
        UUID id,
        boolean isNewUser,
        Instant createdAt
) {
}