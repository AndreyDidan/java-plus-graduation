package ru.practicum.configuration;

import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Map;
import java.util.Properties;

@Getter
@Configuration
@EnableConfigurationProperties({KafkaConfigProperties.class})
public class KafkaConfig {

    private final KafkaConfigProperties kafkaProperties;
    private final Map<String, ConsumerProperties> consumers;

    public KafkaConfig(KafkaConfigProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        this.consumers = kafkaProperties.getConsumers();
    }

    public <V> KafkaConsumer<String, V> createConsumer(String consumerKey) {
        ConsumerProperties consumerProperties = consumers.get(consumerKey);
        if (consumerProperties == null) {
            throw new IllegalArgumentException("Consumer config not found for key: " + consumerKey);
        }
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProperties.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerProperties.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProperties.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProperties.getValueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProperties.getEnableAutoCommit());
        return new KafkaConsumer<>(props);
    }

    public KafkaConsumer<String, UserActionAvro> getUserActionConsumer() {
        return createConsumer(ConsumerType.USER_ACTION.getKey());
    }

    public KafkaConsumer<String, EventSimilarityAvro> getEventSimilarityConsumer() {
        return createConsumer(ConsumerType.EVENT_SIMILARITY.getKey());
    }

    public String getTopic(ConsumerType type) {
        return switch (type) {
            case USER_ACTION -> kafkaProperties.getUserActionTopic();
            case EVENT_SIMILARITY -> kafkaProperties.getEventsSimilarityTopic();
        };
    }

    public long getAttemptTimeout(ConsumerType type) {
        return consumers.get(type.getKey()).getAttemptTimeout();
    }
}