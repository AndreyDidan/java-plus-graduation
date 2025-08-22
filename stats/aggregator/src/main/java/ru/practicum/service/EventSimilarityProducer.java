package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.configuration.KafkaTopicResolver;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProducer {

    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaTopicResolver kafkaTopicResolver;

    public void send(EventSimilarityAvro eventSimilarityAvro) {
        String topic = kafkaTopicResolver.getEventSimilarityTopic();
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic,
                null,
                eventSimilarityAvro.getTimestamp().toEpochMilli(),
                String.valueOf(eventSimilarityAvro.getEventA()),
                eventSimilarityAvro
        );
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке сообщения в Kafka", exception);
            } else {
                log.info("Сообщение отправлено в Kafka: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}