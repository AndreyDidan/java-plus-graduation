package ru.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "spring.kafka.topics")
public class KafkaTopics {
    private String userActionsTopic;
    private String eventsSimilarityTopic;
}
