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

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    private static final long EVENT_COUNT_PREDICTION = 5;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    @Override
    public List<RecommendedEventProto> generateRecommendationsForUser(UserPredictionsRequestProto request) {
        List<UserAction> lastUserEvents = userActionRepository.findByUserIdOrderByCreatedDescLimitedTo(
                request.getUserId(), request.getMaxResults()
        );

        if (lastUserEvents.isEmpty()) {
            return emptyList();
        }

        List<RecommendedEvent> recommendedEvents = new ArrayList<>();
        lastUserEvents.forEach(event -> recommendedEvents.addAll(
                getSimilarEvents(request.getUserId(), event.getEventId(), request.getMaxResults())
                        .stream()
                        .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                        .limit(request.getMaxResults())
                        .map(similarEvent -> genRecommendedEventFrom(similarEvent, event.getEventId()))
                        .toList()));

        List<RecommendedEvent> limitRecommendedEvents = recommendedEvents.stream()
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();
        log.info("RecommendedEvents: {}", recommendedEvents);
        limitRecommendedEvents.forEach(
                event -> event.setScore(getPrediction(event.getEventId(), request.getUserId()))
        );
        return limitRecommendedEvents.stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        return getSimilarEvents(request.getUserId(), request.getEventId(), request.getMaxResults()).stream()
                .map(event -> genRecommendedEventProtoFrom(event, request.getEventId()))
                .toList();
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return userActionRepository.getSumWeightForEvents(request.getEventIdList())
                .stream()
                .map(Mapper::mapToRecommendedEventProto)
                .toList();
    }

    @Override
    public void saveUserAction(UserActionAvro userActionAvro) {
        UserAction userAction = Mapper.mapToUserAction(userActionAvro);
        log.info("call saveUserAction for userActionAvro: {}", userActionAvro);
        Optional<UserAction> oldUserAction = userActionRepository.findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());
        if (oldUserAction.isPresent()) {
            userAction.setId(oldUserAction.get().getId());
            if (userAction.getWeight() < oldUserAction.get().getWeight()) {
                userAction.setWeight(oldUserAction.get().getWeight());
            }
        }
        log.info("new UserAction is: {}", userAction);
        userActionRepository.save(userAction);
    }

    private RecommendedEvent genRecommendedEventFrom(EventSimilarity eventSimilarity, Long eventId) {
        Long recommendedEventId = Objects.equals(eventSimilarity.getAeventId(), eventId) ?
                eventSimilarity.getBeventId() : eventSimilarity.getAeventId();

        return RecommendedEvent.builder()
                .eventId(recommendedEventId)
                .score(eventSimilarity.getScore())
                .build();
    }

    private RecommendedEventProto genRecommendedEventProtoFrom(EventSimilarity eventSimilarity, Long eventId) {
        Long recommendedEventId = Objects.equals(eventSimilarity.getAeventId(), eventId) ?
                eventSimilarity.getBeventId() : eventSimilarity.getAeventId();

        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedEventId)
                .setScore(eventSimilarity.getScore())
                .build();
    }

    private List<EventSimilarity> getSimilarEvents(Long userId, Long eventId, Long limit) {
        List<EventSimilarity> events = eventSimilarityRepository.findAllByEvent(eventId);
        List<Long> actions = userActionRepository.findAllByUserId(userId).stream()
                .map(UserAction::getEventId).toList();

        List<EventSimilarity> result = events.stream()
                .filter(event -> !(actions.contains(event.getAeventId()) && actions.contains(event.getBeventId())))
                .sorted(Comparator.comparingDouble(EventSimilarity::getScore).reversed())
                .limit(limit)
                .toList();

        log.info("similar events are {}", result);
        return result;
    }

    private double getPrediction(Long eventId, Long userId) {
        double prediction = 0.0;

        Map<Long, Double> ratedEvents = userActionRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));
        List<RecommendedEvent> similarEvents = eventSimilarityRepository.findAllByEventAndEventIdInLimitedTo(
                        eventId, ratedEvents.keySet().stream().toList(), EVENT_COUNT_PREDICTION)
                .stream()
                .map(eventSimilarity -> genRecommendedEventFrom(eventSimilarity, eventId))
                .toList();

        double weightedSum = 0.0;
        double similaritySum = 0.0;

        for (RecommendedEvent event : similarEvents) {
            weightedSum += event.getScore() * ratedEvents.get(event.getEventId());
            similaritySum += event.getScore();
        }

        if (similaritySum != 0) {
            prediction = weightedSum / similaritySum;
        }

        return prediction;
    }
}