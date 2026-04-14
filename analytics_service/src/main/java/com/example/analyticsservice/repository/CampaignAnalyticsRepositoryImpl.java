package com.example.analyticsservice.repository;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.example.analyticsservice.domain.document.CampaignAnalytics;
import com.example.analyticsservice.domain.enums.AnalyticsStatus;

@Repository
public class CampaignAnalyticsRepositoryImpl implements CampaignAnalyticsRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public CampaignAnalyticsRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<CampaignAnalytics> initializeTracking(UUID eventId, UUID campaignId, Instant startedAt) {
        Query query = Query.query(Criteria.where("_id").is(campaignId).and("processedEventIds").ne(eventId));
        Update resetUpdate = new Update()
                .set("campaignId", campaignId)
                .set("impressions", 0L)
                .set("clicks", 0L)
                .set("conversions", 0L)
                .set("status", AnalyticsStatus.ACTIVE)
                .set("startedAt", startedAt)
                .unset("endedAt")
                .addToSet("processedEventIds", eventId);

        CampaignAnalytics updated = mongoTemplate.findAndModify(
                query,
                resetUpdate,
                FindAndModifyOptions.options().returnNew(true),
                CampaignAnalytics.class
        );
        if (updated != null) {
            return Optional.of(updated);
        }

        if (mongoTemplate.exists(Query.query(Criteria.where("_id").is(campaignId)), CampaignAnalytics.class)) {
            return Optional.empty();
        }

        // Initialize tracking with an upsert so the first activation can create the analytics document lazily.
        Update createUpdate = new Update()
                .setOnInsert("campaignId", campaignId)
                .setOnInsert("impressions", 0L)
                .setOnInsert("clicks", 0L)
                .setOnInsert("conversions", 0L)
                .setOnInsert("status", AnalyticsStatus.ACTIVE)
                .setOnInsert("startedAt", startedAt)
                .setOnInsert("processedEventIds", new LinkedHashSet<>())
                .addToSet("processedEventIds", eventId);
        mongoTemplate.upsert(Query.query(Criteria.where("_id").is(campaignId)), createUpdate, CampaignAnalytics.class);
        return Optional.ofNullable(mongoTemplate.findById(campaignId, CampaignAnalytics.class));
    }

    @Override
    public Optional<CampaignAnalytics> completeTracking(UUID eventId, UUID campaignId, Instant endedAt) {
        Query query = Query.query(Criteria.where("_id").is(campaignId).and("processedEventIds").ne(eventId));
        Update update = new Update()
                .set("status", AnalyticsStatus.COMPLETED)
                .set("endedAt", endedAt)
                .addToSet("processedEventIds", eventId);

        CampaignAnalytics updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                CampaignAnalytics.class
        );
        return Optional.ofNullable(updated);
    }

    @Override
    public Optional<CampaignAnalytics> incrementMetric(UUID eventId, UUID campaignId, AnalyticsMetric metric, Instant occurredAt) {
        Query query = Query.query(Criteria.where("_id").is(campaignId)
                .and("status").is(AnalyticsStatus.ACTIVE)
                .and("processedEventIds").ne(eventId));

        // The metric increment and event-id registration happen in one atomic Mongo update to avoid duplicate counts.
        Update update = new Update()
                .inc(metric.fieldName(), 1L)
                .addToSet("processedEventIds", eventId);

        CampaignAnalytics updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                CampaignAnalytics.class
        );
        return Optional.ofNullable(updated);
    }
}