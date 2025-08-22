package ru.practicum.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaTopicResolver {
    private final KafkaConfiguration kafkaConfig;

    public String getUserActionsTopic() {
        return kafkaConfig.getKafkaProperties()
                .getConsumer()
                .getTopics()
                .get(TopicKeys.USER_ACTIONS);
    }

    public String getEventSimilarityTopic() {
        return kafkaConfig.getKafkaProperties()
                .getProducer()
                .getTopics()
                .get(TopicKeys.EVENT_SIMILARITY);
    }
}