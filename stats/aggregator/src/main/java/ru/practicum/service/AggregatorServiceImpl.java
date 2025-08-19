/*package ru.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final Map<Long, Map<Long, Double>> eventUserWeightMap = new HashMap<>();
    private final Map<Long, Double> eventSumm = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventMinSumm = new HashMap<>();

    private final Set<String> sentPairs = new HashSet<>();

    @Override
    public List<EventSimilarityAvro> getSimilarities(UserActionAvro actionAvro) {
        log.info("Сервис AggregatorServiceImpl.getSimilarities");

        long eventId = actionAvro.getEventId();
        long userId = actionAvro.getUserId();
        double newScore = getActionScore(actionAvro);

        Map<Long, Double> userWeights = eventUserWeightMap
                .computeIfAbsent(eventId, k -> new HashMap<>());
        double currentWeight = userWeights.getOrDefault(userId, 0.0);

        if (currentWeight >= newScore) {
            log.info("Старый вес >= нового, ничего не пересчитываем");
            return List.of();
        }

        userWeights.put(userId, newScore);
        double newEventScoreSum = eventSumm.getOrDefault(eventId, 0.0) - currentWeight + newScore;
        eventSumm.put(eventId, newEventScoreSum);

        Map<Long, Double> eventsToRecalculate = getLongDoubleMap(actionAvro, eventId);
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : eventsToRecalculate.entrySet()) {
            long otherEventId = entry.getKey();
            double otherWeight = entry.getValue();

            double minSum = getMinScore(eventId, otherEventId);
            double deltaMin = Math.min(newScore, otherWeight) - Math.min(currentWeight, otherWeight);
            if (deltaMin != 0) {
                minSum += deltaMin;
                putMinWeights(eventId, otherEventId, minSum);
            }

            double otherSum = eventSumm.getOrDefault(otherEventId, 0.0);
            if (otherSum == 0.0) {
                continue;
            }

            float score = (float) (minSum / Math.sqrt(newEventScoreSum) / Math.sqrt(otherSum));

            long a = Math.min(eventId, otherEventId);
            long b = Math.max(eventId, otherEventId);
            String pairKey = a + ":" + b;

            if (sentPairs.contains(pairKey)) {
                continue;
            }

            sentPairs.add(pairKey);

            similarities.add(
                    EventSimilarityAvro.newBuilder()
                            .setEventA(a)
                            .setEventB(b)
                            .setTimestamp(actionAvro.getTimestamp())
                            .setScore(score)
                            .build()
            );
        }

        log.info("Сформированы {} пар схожести.", similarities.size());
        return similarities;
    }

    private Map<Long, Double> getLongDoubleMap(UserActionAvro action, long eventId) {
        Map<Long, Double> result = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeightMap.entrySet()) {
            Long otherEvent = entry.getKey();
            Map<Long, Double> weights = entry.getValue();
            if (!otherEvent.equals(eventId) && weights.containsKey(action.getUserId())) {
                result.put(otherEvent, weights.get(action.getUserId()));
            }
        }
        return result;
    }

    private void putMinWeights(long eventA, long eventB, double sum) {
        long a = Math.min(eventA, eventB);
        long b = Math.max(eventA, eventB);
        eventMinSumm
                .computeIfAbsent(a, k -> new HashMap<>())
                .put(b, sum);
    }

    private double getMinScore(long eventA, long eventB) {
        long a = Math.min(eventA, eventB);
        long b = Math.max(eventA, eventB);
        Map<Long, Double> inner = eventMinSumm.get(a);
        return inner == null ? 0.0 : inner.getOrDefault(b, 0.0);
    }

    private double getActionScore(UserActionAvro action) {
        return switch (action.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}*/