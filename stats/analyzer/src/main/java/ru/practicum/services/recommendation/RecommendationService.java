package ru.practicum.services.recommendation;

import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationService {
    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto eventsRequestProto);

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto requestProto);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
