package ru.practicum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "analyzer.kafka.config")
public class KafkaConfigProperties {
    private String bootstrapServers;
    private ConsumerProperties userActionConsumer;
    private ConsumerProperties eventSimilarityConsumer;
    private String userActionTopic;
    private String eventsSimilarityTopic;
}