package com.example.analyticsservice.domain.document;

import com.example.analyticsservice.domain.enums.AnalyticsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "campaign_analytics")
public class CampaignAnalytics {

    @Id
    private UUID campaignId;

    private long impressions;

    private long clicks;

    private long conversions;

    private AnalyticsStatus status;

    private Instant startedAt;

    private Instant endedAt;

    @Builder.Default
    private Set<UUID> processedEventIds = new LinkedHashSet<>();
}