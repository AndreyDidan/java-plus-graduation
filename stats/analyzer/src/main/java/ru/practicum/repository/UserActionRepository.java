package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);

    List<UserAction> findAllByUserId(Long userId);

    @Query("SELECT new ru.practicum.model.RecommendedEvent(ua.eventId, sum(ua.weight)) " +
            "FROM UserAction ua WHERE ua.eventId in :ids GROUP BY ua.eventId")
    List<RecommendedEvent> getSumWeightForEvents(@Param("ids") List<Long> ids);

    @Query("SELECT ua FROM UserAction ua WHERE ua.userId = :id ORDER BY ua.created DESC LIMIT :limit")
    List<UserAction> findByUserIdOrderByCreatedDescLimitedTo(@Param("id") Long userId, @Param("limit") long limit);
}