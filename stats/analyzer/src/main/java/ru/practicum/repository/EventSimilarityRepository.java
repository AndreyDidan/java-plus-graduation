package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    Optional<EventSimilarity> findEventSimilaritiesByEventAAndEventB(Long eventA, Long eventB);

    List<EventSimilarity> findAllByEventAOrEventB(Long eventA, Long eventB);

    List<EventSimilarity> findAllByEventAInOrEventBIn(List<Long> eventIdsA, List<Long> eventIdsB);
}
