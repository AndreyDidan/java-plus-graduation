package ru.practicum.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.EventRequest;

import java.util.Collection;

public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {

    Collection<EventRequest> findByRequesterId(Long requesterId);

    Collection<EventRequest> findByEventId(Long eventId);

    EventRequest findByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("""
        SELECT r
        FROM EventRequest AS r
        WHERE id IN ?1
    """)
    Collection<EventRequest> findById(Collection<Long> requestsIds);
}
