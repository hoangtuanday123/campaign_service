package com.example.campaignservice.dto.campaign;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record CreateCampaignRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotNull(message = "startTime is required")
        Instant startTime,
        @NotNull(message = "endTime is required")
        @Future(message = "endTime must be in the future")
        Instant endTime,
        @Positive(message = "quota must be greater than zero")
        int quota,
        @NotBlank(message = "ruleId is required")
        String ruleId
) {

    @AssertTrue(message = "endTime must be after startTime")
    public boolean isScheduleValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }
}