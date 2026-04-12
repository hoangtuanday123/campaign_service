package com.example.promotionengine.dto.user;

import java.util.UUID;

public record UserSnapshot(
        UUID id,
        String status
) {

    public boolean isActive() {
        return status == null || !"INACTIVE".equalsIgnoreCase(status);
    }
}