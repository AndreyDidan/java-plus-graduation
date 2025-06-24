package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.EventRequestDtoMapper;
import ru.practicum.request.model.EventRequest;
import ru.practicum.request.model.EventRequestStatus;
import ru.practicum.request.storage.EventRequestRepository;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("eventRequestServiceImpl")
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {
    private final UserService userService;
    private final EventService eventService;
    private final EventRequestRepository eventRequestRepository;
    private final EventRequestDtoMapper eventRequestDtoMapper;

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        final UserDto user = userService.findById(userId);
        final EventFullDto event = eventService.findById(userId, eventId, false, null);
        final EventRequest foundOldRequest = eventRequestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (foundOldRequest != null) {
            throw new ConflictException("Trying to create already exist request");
        }

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Initiator of event can't be the same with requester");
        }
        validateEventForRequest(event);

        EventRequestStatus status = (event.getParticipantLimit().equals(0L) || !event.getRequestModeration()) ?
                EventRequestStatus.CONFIRMED : EventRequestStatus.PENDING;

        final EventRequest request = new EventRequest(
                null,
                eventId,
                userId,
                status,
                LocalDateTime.now()
        );
        final EventRequest createdRequest = eventRequestRepository.save(request);
        if (status.equals(EventRequestStatus.CONFIRMED)) {
            eventService.updateEventConfirmedRequests(event.getId(), event.getConfirmedRequests() + 1);
        }

        return eventRequestDtoMapper.mapToResponseDto(createdRequest);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestsToUpdate) {
        final UserDto user = userService.findById(userId);
        final EventFullDto event = eventService.findById(userId, eventId, false, null);

        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Not initiator of event can't be change status of requests");
        }
        validateEventForRequest(event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        final Collection<EventRequest> requests = eventRequestRepository.findById(requestsToUpdate.getRequestIds());
        validateRequests(requests);

        switch (requestsToUpdate.getStatus()) {
            case REJECTED -> rejectRequests(result, requests);
            case CONFIRMED -> confirmRequests(result, event, requests);
            default -> throw new ForbiddenException("Unknown state to update");
        }

        return result;
    }

    @Override
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        final EventRequest request = eventRequestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Request with id=" + requestId + " was not found")
        );
        if (!userId.equals(request.getRequesterId())) {
            throw new NotFoundException("Not owner (userId=" + userId + ") of request trying to cancel it (request=" + request + ")");
        }
        request.setStatus(EventRequestStatus.CANCELED);
        final EventRequest updatedRequest = eventRequestRepository.save(request);
        return eventRequestDtoMapper.mapToResponseDto(updatedRequest);
    }

    @Override
    public Collection<ParticipationRequestDto> getByRequesterId(Long requesterId) {
        if (userService.findById(requesterId) == null) {
            throw new NotFoundException("User with id=" + requesterId + " was not found");
        }
        final Collection<EventRequest> requests = eventRequestRepository.findByRequesterId(requesterId);
        return requests.stream()
                .map(eventRequestDtoMapper::mapToResponseDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<ParticipationRequestDto> getByEventId(Long eventInitiatorId, Long eventId) {
        if (userService.findById(eventInitiatorId) == null) {
            throw new NotFoundException("User with id=" + eventInitiatorId + " was not found");
        }
        if (eventService.findById(eventInitiatorId, eventId, false, null) == null) {
            throw new NotFoundException("Event with id=" + eventId + " was not found on user with id=" + eventInitiatorId);
        }
        final Collection<EventRequest> requests = eventRequestRepository.findByEventId(eventId);
        return requests.stream()
                .map(eventRequestDtoMapper::mapToResponseDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void validateEventForRequest(EventFullDto event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Can't send request to unpublished event");
        }
        if (event.getParticipantLimit().equals(0L)) {
            return;
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Limit of event can't be full");
        }
    }

    private void validateRequests(Collection<EventRequest> requests) {
        for (EventRequest request : requests) {
            if (!request.getStatus().equals(EventRequestStatus.PENDING)) {
                throw new ConflictException("Trying to change status of not pending request");
            }
        }
    }

    private void rejectRequests(EventRequestStatusUpdateResult result, Collection<EventRequest> requests) {
        for (EventRequest request : requests) {
            request.setStatus(EventRequestStatus.REJECTED);
            final EventRequest updatedRequest = eventRequestRepository.save(request);
            result.getRejectedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
        }
    }

    private void confirmRequests(EventRequestStatusUpdateResult result, EventFullDto event, Collection<EventRequest> requests) {
        final Long limit = event.getParticipantLimit();

        Long currentConfirmed = event.getConfirmedRequests();
        for (EventRequest request : requests) {
            if (currentConfirmed >= limit) {
                request.setStatus(EventRequestStatus.REJECTED);
                final EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getRejectedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
            } else {
                request.setStatus(EventRequestStatus.CONFIRMED);
                final EventRequest updatedRequest = eventRequestRepository.save(request);
                result.getConfirmedRequests().add(eventRequestDtoMapper.mapToResponseDto(updatedRequest));
                currentConfirmed++;
            }
        }

        eventService.updateEventConfirmedRequests(event.getId(), currentConfirmed);
    }
}
