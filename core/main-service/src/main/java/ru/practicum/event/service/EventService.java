package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;

import java.util.Collection;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto eventDto);

    Collection<EventShortDto> findAllByPublic(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request);

    Collection<EventShortDto> findAllByPrivate(Long userId, Integer from, Integer size);

    Collection<EventFullDto> findAllByAdmin(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto findById(Long userId, Long eventId, Boolean isPublic, HttpServletRequest request);

    EventFullDto updateByPrivate(Long userId, Long eventId, UpdateEventUserRequest eventDto);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest eventDto);

    void updateEventConfirmedRequests(Long eventId, Long confirmedRequests);
}
