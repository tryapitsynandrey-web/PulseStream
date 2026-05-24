package com.pulsestream.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentConfirmedTopic() {
        return TopicBuilder.name("payment-confirmed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundIssuedTopic() {
        return TopicBuilder.name("refund-issued")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic activityDetectedTopic() {
        return TopicBuilder.name("activity-detected")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
