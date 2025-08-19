package ru.practicum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka.config")
public class KafkaProperties {
    private String bootstrapServers;

    private String producerClientIdConfig;
    private String producerKeySerializer;
    private String producerValueSerializer;

    private String consumerGroupId;
    private String consumerClientIdConfig;
    private String consumerKeyDeserializer;
    private String consumerValueDeserializer;
    private long consumerAttemptTimeout;
    private String consumerEnableAutoCommit;

    private String userActionTopic;
    private String eventsSimilarityTopic;
}