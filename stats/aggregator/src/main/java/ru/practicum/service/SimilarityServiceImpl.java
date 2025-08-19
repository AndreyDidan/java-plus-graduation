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

    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>(); //Map<eventId, Map<userId, Weight>>
    private final Map<Long, Double> eventSummaryWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventMinSummaryWeights = new HashMap<>();

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
        if (oldWeight != newWeight) {
            log.info("starting update similarity");
            eventSummaryWeights.put(eventId, eventSummaryWeights.getOrDefault(eventId, 0.0) + newWeight - oldWeight);
            log.info("eventSummaryWeights updated: new summary weight for eventId = {} equals {}", eventId,
                    eventSummaryWeights.get(eventId));
            reCalcEventMinSummaryWeights(eventId, userId);

            for (Long secondEvent : eventWeights.keySet()) {
                if (!eventId.equals(secondEvent)) {
                    long eventA = Math.min(eventId, secondEvent);
                    long eventB = Math.max(eventId, secondEvent);
                    double sumWeightA = eventSummaryWeights.get(eventA);
                    double sumWeightB = eventSummaryWeights.get(eventB);
                    log.info("eventA = {}, eventB = {}, sumWeightA = {}, sumWeightB = {}", eventA, eventB,
                            sumWeightA, sumWeightB);
                    if (sumWeightA + sumWeightB != 0) {
                        double score = getEventMinSummaryWeights(eventA, eventB) /
                                (Math.sqrt(sumWeightA) * Math.sqrt(sumWeightB));
                        log.info("SCORE = {}", score);
                        EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                                .setEventA(eventA)
                                .setEventB(eventB)
                                .setScore(score)
                                .setTimestamp(userAction.getTimestamp())
                                .build();
                        log.info("NEW EventSimilarityAvro = {}", eventSimilarity);
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

    private void putEventMinSummaryWeights(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        eventMinSummaryWeights
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double getEventMinSummaryWeights(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return eventMinSummaryWeights
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private double getWeightByActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    //return old weight
    private double addOrUpdateEventWeightForUser(Long eventId, Long userId, double weight) {
        double oldWeight = 0.0;
        eventWeights.computeIfAbsent(eventId, e -> new HashMap<>()).putIfAbsent(userId, 0.0);
        if (eventWeights.containsKey(eventId)) {
            oldWeight = eventWeights.get(eventId).get(userId);
            double maxWeight = Math.max(oldWeight, weight);
            if (oldWeight != maxWeight) {
                eventWeights.get(eventId).put(userId, maxWeight);
                log.info("eventWeights updated for eventId = {}: new weight = {}", eventId, maxWeight);
            }
        }
        return oldWeight;
    }

    private void reCalcEventMinSummaryWeights(Long eventId, Long userId) {
        double weightForThisEvent = eventWeights.get(eventId).getOrDefault(userId, 0.0);
        for (Long otherEvent : eventWeights.keySet()) {
            if (!Objects.equals(otherEvent, eventId)) {
                double weightForOtherEvent = eventWeights.get(otherEvent).getOrDefault(userId, 0.0);
                double newMinWeight = Math.min(weightForThisEvent, weightForOtherEvent);
                double oldMinWeight = getEventMinSummaryWeights(eventId, otherEvent);
                putEventMinSummaryWeights(eventId, otherEvent, newMinWeight - oldMinWeight);
            }
        }
    }
}