package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventSimilarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findByAeventIdAndBeventId(Long aEventId, Long bEventId);

    @Query("select es from EventSimilarity es where es.aeventId = :id or es.beventId = :id")
    List<EventSimilarity> findAllByEvent(@Param("id") Long eventId);

    @Query("select es from EventSimilarity es " +
            " where (es.aeventId = :id and es.beventId in :ids) or " +
            " (es.beventId = :id and es.aeventId in :ids) " +
            " order by es.score desc" +
            " limit :limit")
    List<EventSimilarity> findAllByEventAndEventIdInLimitedTo(
            @Param("id") Long eventId,
            @Param("ids") List<Long> eventIds,
            @Param("limit") Long limit);
}