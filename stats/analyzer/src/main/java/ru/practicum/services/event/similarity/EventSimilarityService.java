package ru.practicum.services.event.similarity;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityService {
    void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro);
}
