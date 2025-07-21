package ru.practicum.service;

import ru.practicum.model.EventRequestStatusUpdateRequest;
import ru.practicum.model.EventRequestStatusUpdateResult;
import ru.practicum.model.ParticipationRequestDto;

import java.util.Collection;

public interface EventRequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestsToUpdate);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    Collection<ParticipationRequestDto> getByRequesterId(Long requesterId);

    Collection<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId);
}

