package ru.practicum.configuration;

import lombok.Data;

import java.util.Map;

@Data
public class KafkaConsumerProperties {
    private String groupId;
    private String clientIdConfig;
    private String keyDeserializer;
    private String valueDeserializer;
    private long attemptTimeout;
    private String enableAutoCommit;
    private Map<String, String> topics;
}