package ru.practicum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "spring.kafka.config")
public class KafkaProperties {
    private String bootstrapServers;

    @NestedConfigurationProperty
    private KafkaProducerProperties producer;

    @NestedConfigurationProperty
    private KafkaConsumerProperties consumer;
}