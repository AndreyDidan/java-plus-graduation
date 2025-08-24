package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.mapper.Mapper;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final long EVENT_COUNT_PREDICTION = 5;

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public List<RecommendedEventProto> generateRecommendationsForUser(UserPredictionsRequestProto request) {
        log.info("Start generating recommendations for user: {}, maxResults: {}", request.getUserId(), request.getMaxResults());

        List<UserAction> lastUserActions = userActionRepository
                .findByUserIdOrderByCreatedDescLimitedTo(request.getUserId(), request.getMaxResults());

        if (lastUserActions.isEmpty()) {
            log.warn("No recent actions found for user: {}", request.getUserId());
            return Collections.emptyList();
        }

        Set<Long> interactedEventIds = userActionRepository.findAllByUserId(request.getUserId()).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        Map<Long, Double> ratedEvents = userActionRepository.findAllByUserId(request.getUserId()).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight, (a, b) -> b));

        // Список ID событий из последних действий пользователя
        Set<Long> baseEventIds = lastUserActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        // Получаем все похожести одним запросом
        List<EventSimilarity> allSimilarities = eventSimilarityRepository.findAllByEventIn(baseEventIds);

        Map<Long, Double> eventWeights = lastUserActions.stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));

        Map<Long, Double> aggregatedScores = new HashMap<>();

        for (EventSimilarity similarity : allSimilarities) {
            Long a = similarity.getAeventId();
            Long b = similarity.getBeventId();

            Long baseEventId = baseEventIds.contains(a) ? a : b;
            Long similarEventId = baseEventId.equals(a) ? b : a;

            if (interactedEventIds.contains(similarEventId)) continue;

            double userWeight = eventWeights.getOrDefault(baseEventId, 0.0);
            double weightedScore = similarity.getScore() * userWeight;

            aggregatedScores.merge(similarEventId, weightedScore, Double::sum);
        }

        List<RecommendedEvent> predictedEvents = aggregatedScores.entrySet().stream()
                .map(entry -> RecommendedEvent.builder()
                        .eventId(entry.getKey())
                        .score(getPrediction(entry.getKey(), ratedEvents))
                        .build())
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();

        log.info("Returning {} recommended events", predictedEvents.size());

        return predictedEvents.stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Getting similar events for eventId: {}, userId: {}", request.getEventId(), request.getUserId());

        Set<Long> interactedEventIds = userActionRepository.findAllByUserId(request.getUserId()).stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        List<EventSimilarity> similarities = eventSimilarityRepository.findAllByEvent(request.getEventId());

        return similarities.stream()
                .map(similarity -> {
                    Long similarEventId = getRecommendedEventId(similarity, request.getEventId());
                    return new AbstractMap.SimpleEntry<>(similarEventId, similarity.getScore());
                })
                .filter(entry -> !interactedEventIds.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Getting interactions count for eventIds: {}", request.getEventIdList());

        return userActionRepository.getSumWeightForEvents(request.getEventIdList()).stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public void saveUserAction(UserActionAvro userActionAvro) {
        UserAction userAction = Mapper.mapToUserAction(userActionAvro);

        Optional<UserAction> existing = userActionRepository
                .findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());

        existing.ifPresent(old -> {
            userAction.setId(old.getId());
            if (userAction.getWeight() < old.getWeight()) {
                userAction.setWeight(old.getWeight());
            }
        });

        userActionRepository.save(userAction);
        log.debug("Saved user action: {}", userAction);
    }

    private Long getRecommendedEventId(EventSimilarity similarity, Long referenceEventId) {
        return Objects.equals(similarity.getAeventId(), referenceEventId)
                ? similarity.getBeventId()
                : similarity.getAeventId();
    }

    /**
     * Метод не используется после оптимизации, но можно оставить для будущих задач
     */
    private double getPrediction(Long targetEventId, Map<Long, Double> ratedEvents) {
        List<EventSimilarity> similarities = eventSimilarityRepository
                .findAllByEventAndEventIdInLimitedTo(targetEventId, new ArrayList<>(ratedEvents.keySet()), EVENT_COUNT_PREDICTION);

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (EventSimilarity similarity : similarities) {
            Long similarEventId = getRecommendedEventId(similarity, targetEventId);
            Double rating = ratedEvents.get(similarEventId);
            if (rating != null) {
                weightedSum += similarity.getScore() * rating;
                similaritySum += similarity.getScore();
            }
        }

        return similaritySum == 0.0 ? 0.0 : weightedSum / similaritySum;
    }
}