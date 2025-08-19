package ru.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "analyzer.kafka.config")
public class KafkaConfigProperties {
    private String bootstrapServers;
    private ConsumerProperties userActionConsumer;
    private ConsumerProperties eventSimilarityConsumer;

    private String userActionTopic;
    private String eventsSimilarityTopic;
}