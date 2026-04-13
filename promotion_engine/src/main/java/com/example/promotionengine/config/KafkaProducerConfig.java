package com.example.promotionengine.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PromotionEventProperties.class)
public class KafkaProducerConfig {
}