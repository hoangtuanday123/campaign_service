package com.example.campaignservice.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import com.example.campaignservice.dto.event.CampaignEvent;

@Configuration
public class KafkaProducerConfig {

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory<String, CampaignEvent> campaignEventProducerFactory(Environment environment) {
        Binder binder = Binder.get(environment);
        Map<String, Object> properties = new HashMap<>();
        List<String> bootstrapServers = binder.bind("spring.kafka.bootstrap-servers", Bindable.listOf(String.class))
                .orElse(List.of("localhost:9092"));
        Map<String, String> producerProperties = binder.bind(
                        "spring.kafka.producer.properties",
                        Bindable.mapOf(String.class, String.class)
                )
                .orElseGet(Map::of);

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        putIfBound(environment, properties, "spring.kafka.producer.acks", ProducerConfig.ACKS_CONFIG);
        putIfBound(environment, properties, "spring.kafka.producer.retries", ProducerConfig.RETRIES_CONFIG);
        properties.putAll(producerProperties);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    @ConditionalOnMissingBean(KafkaTemplate.class)
    public KafkaTemplate<String, CampaignEvent> campaignEventKafkaTemplate(
            ProducerFactory<String, CampaignEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    private void putIfBound(Environment environment, Map<String, Object> properties, String sourceKey, String targetKey) {
        String value = environment.getProperty(sourceKey);
        if (value != null) {
            properties.put(targetKey, value);
        }
    }
}