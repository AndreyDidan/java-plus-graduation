package ru.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("app.kafka")
public class AppConfig {
    ProducerSettings producer;
    ConsumerSettings consumer;
    TopicsSettings topics;

    @Data
    public static class ProducerSettings {
        private String bootstrapServers;
        private String keySerializer;
        private String valueSerializer;
    }

    @Data
    public static class ConsumerSettings {
        private String bootstrapServers;
        private String keyDeserializer;
        private String valueDeserializer;
        private String clientId;
        private String groupId;
        private String maxPollRecords;
        private String fetchMaxBytes;
        private String maxPartitionFetchBytes;
        private Duration consumeAttemptsTimeoutMs;
    }

    @Data
    public static class TopicsSettings {
        private String actionTopic;
        private String similarityTopic;
    }
}