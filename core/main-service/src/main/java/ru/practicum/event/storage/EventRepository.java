package ru.practicum.event.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    @Query("""
            SELECT e
            FROM Event AS e
            WHERE (?1 IS NULL or e.initiator.id IN ?1)
                AND (?2 IS NULL or e.state IN ?2)
                AND (?3 IS NULL or e.category.id in ?3)
                AND (CAST(?4 AS timestamp) IS NULL or e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL or e.eventDate < ?5)
        """)
    List<Event> findAllByAdmin(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Pageable pageable
    );


    @Query("""
            SELECT e
            FROM Event AS e
            WHERE e.state = PUBLISHED
                AND (?1 IS NULL or e.annotation ILIKE %?1% or e.description ILIKE %?1%)
                AND (?2 IS NULL or e.category.id in ?2)
                AND (?3 IS NULL or e.paid = ?3)
                AND (CAST(?4 AS timestamp) IS NULL and e.eventDate >= CURRENT_TIMESTAMP or e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL or e.eventDate < ?5)
                AND (?6 = false or e.participantLimit = 0 or e.participantLimit < e.confirmedRequests)
        """)
    List<Event> findAllByPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            Pageable pageable
    );

    @Query(value = """
            UPDATE events
            SET confirmedRequests = ?2
            WHERE id = ?1
        """,
        nativeQuery = true
    )
    void updateConfirmedRequests(Long eventId, Long confirmedRequests);

    List<Event> findAllByIdIn(List<Long> eventIds);
}
