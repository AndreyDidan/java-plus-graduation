package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.configuration.KafkaConfiguration;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityServiceImpl implements SimilarityService {
    private final Producer<String, SpecificRecordBase> producer;
    private final KafkaConfiguration kafkaConfig;

    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();
    private final Map<Long, Double> eventSummaryWeights = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> updateSimilarity(UserActionAvro userAction) {
        log.info("updateSimilarity for userAction = {}", userAction);

        List<EventSimilarityAvro> result = new ArrayList<>();

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();
        double receivedWeight = getWeightByActionType(userAction.getActionType());

        double oldWeight = addOrUpdateEventWeightForUser(eventId, userId, receivedWeight);
        double newWeight = Math.max(oldWeight, receivedWeight);

        log.info("receivedWeight = {}, oldWeight = {}, newWeight = {}", receivedWeight, oldWeight, newWeight);

        // Если вес действительно изменился
        if (oldWeight != newWeight) {
            log.info("starting update similarity");

            // Обновляем сумму весов по событию
            eventSummaryWeights.put(eventId,
                    eventSummaryWeights.getOrDefault(eventId, 0.0) + (newWeight - oldWeight));
            log.info("eventSummaryWeights updated: eventId = {}, summaryWeight = {}",
                    eventId, eventSummaryWeights.get(eventId));

            // Пересчитываем similarity с другими событиями, где пользователь уже взаимодействовал
            for (Long otherEvent : eventWeights.keySet()) {
                if (!eventId.equals(otherEvent) && eventWeights.get(otherEvent).containsKey(userId)) {
                    long eventA = Math.min(eventId, otherEvent);
                    long eventB = Math.max(eventId, otherEvent);

                    double sumWeightA = eventSummaryWeights.getOrDefault(eventA, 0.0);
                    double sumWeightB = eventSummaryWeights.getOrDefault(eventB, 0.0);

                    if (sumWeightA > 0 && sumWeightB > 0) {
                        double minSum = computeMinSum(eventA, eventB);
                        double score = minSum / (Math.sqrt(sumWeightA) * Math.sqrt(sumWeightB));

                        log.info("Computed similarity: A={}, B={}, minSum={}, sumA={}, sumB={}, score={}",
                                eventA, eventB, minSum, sumWeightA, sumWeightB, score);

                        EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                                .setEventA(eventA)
                                .setEventB(eventB)
                                .setScore(score)
                                .setTimestamp(userAction.getTimestamp())
                                .build();
                        result.add(eventSimilarity);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void collectEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        ProducerRecord<String, SpecificRecordBase> rec = new ProducerRecord<>(
                kafkaConfig.getKafkaProperties().getEventsSimilarityTopic(),
                null,
                eventSimilarityAvro.getTimestamp().toEpochMilli(),
                String.valueOf(eventSimilarityAvro.getEventA()),
                eventSimilarityAvro);
        producer.send(rec);
    }

    @Override
    public void close() {
        SimilarityService.super.close();
        if (producer != null) {
            producer.close();
        }
    }

    private double getWeightByActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    private double addOrUpdateEventWeightForUser(Long eventId, Long userId, double weight) {
        Map<Long, Double> userWeights = eventWeights.computeIfAbsent(eventId, e -> new HashMap<>());
        double oldWeight = userWeights.getOrDefault(userId, 0.0);
        double maxWeight = Math.max(oldWeight, weight);
        if (oldWeight != maxWeight) {
            userWeights.put(userId, maxWeight);
            log.info("eventWeights updated: eventId={}, userId={}, newWeight={}", eventId, userId, maxWeight);
        }
        return oldWeight;
    }

    /**
     * Вычисляет сумму минимумов весов по всем общим пользователям между eventA и eventB.
     */
    private double computeMinSum(long eventA, long eventB) {
        Map<Long, Double> weightsA = eventWeights.getOrDefault(eventA, Map.of());
        Map<Long, Double> weightsB = eventWeights.getOrDefault(eventB, Map.of());

        double sum = 0.0;
        for (Map.Entry<Long, Double> entry : weightsA.entrySet()) {
            Long userId = entry.getKey();
            if (weightsB.containsKey(userId)) {
                sum += Math.min(entry.getValue(), weightsB.get(userId));
            }
        }
        return sum;
    }
}