package com.example.analyticsservice.config;

import com.example.analyticsservice.dto.event.CampaignEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, CampaignEvent> campaignEventConsumerFactory(Environment environment) {
        Binder binder = Binder.get(environment);
        Map<String, Object> properties = new HashMap<>();
        List<String> bootstrapServers = binder.bind("spring.kafka.bootstrap-servers", Bindable.listOf(String.class))
                .orElse(List.of("localhost:9092"));
        Map<String, String> consumerProperties = binder.bind(
                        "spring.kafka.consumer.properties",
                        Bindable.mapOf(String.class, String.class)
                )
                .orElseGet(Map::of);

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        putIfBound(environment, properties, "spring.kafka.consumer.auto-offset-reset", ConsumerConfig.AUTO_OFFSET_RESET_CONFIG);
        properties.putAll(consumerProperties);

        JsonDeserializer<CampaignEvent> jsonDeserializer = new JsonDeserializer<>(CampaignEvent.class, false);
        jsonDeserializer.addTrustedPackages("com.example.analyticsservice.dto.event");

        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public DefaultErrorHandler campaignEventErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }

    @Bean(name = "campaignEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CampaignEvent> campaignEventKafkaListenerContainerFactory(
            ConsumerFactory<String, CampaignEvent> campaignEventConsumerFactory,
            DefaultErrorHandler campaignEventErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, CampaignEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(campaignEventConsumerFactory);
        factory.setCommonErrorHandler(campaignEventErrorHandler);
        return factory;
    }

    private void putIfBound(Environment environment, Map<String, Object> properties, String sourceKey, String targetKey) {
        String value = environment.getProperty(sourceKey);
        if (value != null) {
            properties.put(targetKey, value);
        }
    }
}