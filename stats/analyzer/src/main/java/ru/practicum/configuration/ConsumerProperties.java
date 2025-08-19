package ru.practicum.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsumerProperties {
    private String groupId;
    private String clientId;
    private String keyDeserializer;
    private String valueDeserializer;
    private long attemptTimeout;
    private String enableAutoCommit;
}