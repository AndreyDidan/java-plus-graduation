package ru.practicum.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsumerProperties {
    private String groupId;
    private String clientId;
    private String keyDeserializer;
    private String valueDeserializer;
    private long attemptTimeout;
    private String enableAutoCommit;
}