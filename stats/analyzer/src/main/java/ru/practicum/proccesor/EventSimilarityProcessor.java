package ru.practicum.proccesor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
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
    private final Consumer<String, EventSimilarityAvro> consumer;
    private final KafkaConfig kafkaConfig;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(List.of(kafkaConfig.getKafkaProperties().getEventsSimilarityTopic()));
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer
                        .poll(Duration.ofMillis(kafkaConfig.getKafkaProperties()
                                .getEventSimilarityConsumer().getAttemptTimeout()));
                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignores) {
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

    private void handleRecord(ConsumerRecord<String, EventSimilarityAvro> consumerRecord) throws InterruptedException {
        log.info("handleRecord {}", consumerRecord);
        EventSimilarity eventSimilarity = Mapper.mapToEventSimilarity(consumerRecord.value());

        eventSimilarityRepository.findByAeventIdAndBeventId(
                eventSimilarity.getAeventId(),
                eventSimilarity.getBeventId()).ifPresent(oldEventSimilarity ->
                eventSimilarity.setId(oldEventSimilarity.getId()));
        eventSimilarityRepository.save(eventSimilarity);
    }

    private void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> consumerRecord,
                               int count,
                               Consumer<String, EventSimilarityAvro> consumer) {
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