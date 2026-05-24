package com.pulsestream.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    // ==========================================================================
    // Standard Kafka Topics
    // ==========================================================================

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

    // ==========================================================================
    // Dead Letter Queue (DLQ) Topics
    // ==========================================================================

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name("order-created.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentConfirmedDltTopic() {
        return TopicBuilder.name("payment-confirmed.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundIssuedDltTopic() {
        return TopicBuilder.name("refund-issued.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic activityDetectedDltTopic() {
        return TopicBuilder.name("activity-detected.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ==========================================================================
    // Error Handling & Recovery Config
    // ==========================================================================

    @Bean
    @SuppressWarnings("null")
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
                log.error("Kafka processing failed on topic '{}', partition '{}', offset '{}'. Routing to DLQ. Reason: {}",
                    record.topic(), record.partition(), record.offset(), exception.getMessage());
                return new TopicPartition(record.topic() + ".DLT", 0);
            });

        // Configurable BackOff: 2 retries (3 total attempts) with a 1-second fixed interval
        BackOff backOff = new FixedBackOff(1000L, 2L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setCommitRecovered(true); // Commit the offset of the failed message after sending to DLQ
        return errorHandler;
    }
}
