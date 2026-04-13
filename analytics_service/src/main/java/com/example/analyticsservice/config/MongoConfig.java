package com.example.analyticsservice.config;

import com.example.analyticsservice.repository.CampaignAnalyticsRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackageClasses = CampaignAnalyticsRepository.class)
public class MongoConfig {
}