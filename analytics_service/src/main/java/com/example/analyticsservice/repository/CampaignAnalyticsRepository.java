package com.example.analyticsservice.repository;

import com.example.analyticsservice.domain.document.CampaignAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface CampaignAnalyticsRepository extends MongoRepository<CampaignAnalytics, UUID>, CampaignAnalyticsRepositoryCustom {
}