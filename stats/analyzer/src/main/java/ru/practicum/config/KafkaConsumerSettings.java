package ru.practicum.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class KafkaConsumerSettings {
    private String bootstrapServers;
    private String keyDeserializer;
    private String valueDeserializer;
    private String clientId;
    private String groupId;
    private String maxPollRecords;
    private int fetchMaxBytes;
    private int maxPartitionFetchBytes;
}
