package ru.practicum.configuration;

import lombok.Data;

import java.util.Map;

@Data
public class KafkaProducerProperties {
    private String clientIdConfig;
    private String keySerializer;
    private String valueSerializer;
    private Map<String, String> topics;
}
