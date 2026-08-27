package com.nhhoang.e_commerce.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events-topic";
    public static final String PRODUCT_SYNC_TOPIC = "product-sync-topic";

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        configs.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 1000);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productSyncTopic() {
        return TopicBuilder.name(PRODUCT_SYNC_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
