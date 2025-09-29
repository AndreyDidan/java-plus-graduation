package ru.practicum.mapper;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.model.ActionType;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;

public class Mapper {

    public static UserAction mapToUserAction(UserActionAvro userActionAvro) {
        return UserAction.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .actionType(toActionType(userActionAvro.getActionType()))
                .created(userActionAvro.getTimestamp())
                .weight(toActionType(userActionAvro.getActionType()).getWeight())
                .build();
    }

    public static ActionType toActionType(ActionTypeAvro actionTypeAvro) {
        return ActionType.valueOf(actionTypeAvro.name());
    }

    public static EventSimilarity mapToEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        return EventSimilarity.builder()
                .aeventId(eventSimilarityAvro.getEventA())
                .beventId(eventSimilarityAvro.getEventB())
                .score(eventSimilarityAvro.getScore())
                .build();

    }

    public static RecommendedEventProto mapToRecommendedEventProto(RecommendedEvent recommendedEvent) {
        return RecommendedEventProto.newBuilder()
                .setEventId(recommendedEvent.getEventId())
                .setScore(recommendedEvent.getScore())
                .build();
    }
}