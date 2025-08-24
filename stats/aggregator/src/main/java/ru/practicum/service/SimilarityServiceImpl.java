package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimilarityServiceImpl implements SimilarityService {
    private final EventSimilarityProducer similarityProducer;
    private final Map<Long, Map<Long, Double>> eventWeights = new HashMap<>();
    private final Map<Long, Double> eventSummaryWeights = new HashMap<>();
    private final Map<Long, Double> minSums = new HashMap<>();
    private final Map<Long, Set<Long>> userEventMap = new HashMap<>();

    private long pairKey(long eventA, long eventB) {
        return eventA * 1_000_000_000L + eventB;
    }

    private List<EventSimilarityAvro> updateSimilarityWithUser(Long eventId, Long userId, double oldWeight, double newWeight, Instant timestamp) {
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        Set<Long> userEvents = userEventMap.getOrDefault(userId, Set.of());

        for (Long otherEventId : userEvents) {
            if (eventId.equals(otherEventId)) continue;

            long eventA = Math.min(eventId, otherEventId);
            long eventB = Math.max(eventId, otherEventId);
            long key = pairKey(eventA, eventB);

            double score = computeUpdatedScore(eventA, eventB, key, userId, eventId, oldWeight);
            if (score < 0) continue; // признак ошибки или пропуска

            similarities.add(buildSimilarity(eventA, eventB, score, timestamp));
        }

        return similarities;
    }

    private boolean shouldCompareWithUser(Long eventId, Long otherEventId, Long userId) {
        if (eventId.equals(otherEventId)) return false;

        Map<Long, Double> otherUsers = eventWeights.get(otherEventId);
        return otherUsers != null && otherUsers.containsKey(userId);
    }

    private double computeUpdatedScore(long eventA, long eventB, long key, Long userId, Long changedEventId, double oldWeight) {
        // Старые веса
        double weightA_old = eventA == changedEventId ? oldWeight : eventWeights.get(eventA).getOrDefault(userId, 0.0);
        double weightB_old = eventB == changedEventId ? oldWeight : eventWeights.get(eventB).getOrDefault(userId, 0.0);
        double oldMin = Math.min(weightA_old, weightB_old);

        // Новые веса
        double weightA = eventWeights.get(eventA).getOrDefault(userId, 0.0);
        double weightB = eventWeights.get(eventB).getOrDefault(userId, 0.0);
        double newMin = Math.min(weightA, weightB);

        // Обновляем сумму минимумов
        double delta = newMin - oldMin;
        double updatedMinSum = minSums.getOrDefault(key, 0.0) + delta;
        minSums.put(key, updatedMinSum);

        double sumA = eventSummaryWeights.getOrDefault(eventA, 0.0);
        double sumB = eventSummaryWeights.getOrDefault(eventB, 0.0);

        if (sumA <= 0 || sumB <= 0) return -1; // ничего не делаем

        return updatedMinSum / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }

    private EventSimilarityAvro buildSimilarity(long eventA, long eventB, double score, Instant timestamp) {
        log.info("Computed similarity: A={}, B={}, score={}", eventA, eventB, score);

        return EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }

    @Override
    public void collectEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        similarityProducer.send(eventSimilarityAvro);
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

    @Override
    public List<EventSimilarityAvro> updateSimilarity(UserActionAvro userAction) {
        log.info("updateSimilarity for userAction = {}", userAction);

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();
        double receivedWeight = getWeightByActionType(userAction.getActionType());

        double oldWeight = addOrUpdateEventWeightForUser(eventId, userId, receivedWeight);
        double newWeight = Math.max(oldWeight, receivedWeight);

        if (oldWeight == newWeight) {
            log.info("Вес не изменился, пересчёт схожести не требуется");
            return List.of();
        }

        updateSummaryWeight(eventId, newWeight - oldWeight);
        return updateSimilarityWithUser(eventId, userId, oldWeight, newWeight, userAction.getTimestamp());
    }

    private void updateSummaryWeight(Long eventId, double delta) {
        eventSummaryWeights.put(eventId,
                eventSummaryWeights.getOrDefault(eventId, 0.0) + delta);
        log.info("eventSummaryWeights updated: eventId = {}, delta = {}, newSummary = {}",
                eventId, delta, eventSummaryWeights.get(eventId));
    }
}