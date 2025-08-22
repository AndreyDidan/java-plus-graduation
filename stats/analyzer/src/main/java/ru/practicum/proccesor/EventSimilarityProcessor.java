package ru.practicum.proccesor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.configuration.ConsumerType;
import ru.practicum.configuration.KafkaConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapper.Mapper;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {

    private final KafkaConfig kafkaConfig;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        KafkaConsumer<String, EventSimilarityAvro> consumer = kafkaConfig
                .createConsumer(ConsumerType.EVENT_SIMILARITY.getKey());
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic(ConsumerType.EVENT_SIMILARITY)));
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfig.getAttemptTimeout(ConsumerType.EVENT_SIMILARITY)));
                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки события похожести ", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }

    private void handleRecord(ConsumerRecord<String, EventSimilarityAvro> consumerRecord) {
        log.info("handleRecord {}", consumerRecord);
        EventSimilarity eventSimilarity = Mapper.mapToEventSimilarity(consumerRecord.value());
        eventSimilarityRepository.findByAeventIdAndBeventId(
                        eventSimilarity.getAeventId(), eventSimilarity.getBeventId())
                .ifPresent(old -> eventSimilarity.setId(old.getId()));
        eventSimilarityRepository.save(eventSimilarity);
    }

    private void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> consumerRecord, int count, Consumer<String, EventSimilarityAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(consumerRecord.topic(), consumerRecord.partition()),
                new OffsetAndMetadata(consumerRecord.offset() + 1)
        );
        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}