package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.mapper.EventDtoMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.ewm.CollectorClient;
import ru.practicum.ewm.RecommendationsClient;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.RequestClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.location.mapper.LocationDtoMapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.service.LocationService;
import ru.practicum.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("eventServiceImpl")
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserClient userClient;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final EventRepository eventRepository;
    private final EventDtoMapper eventDtoMapper;
    private final CategoryDtoMapper categoryDtoMapper;
    private final LocationDtoMapper locationDtoMapper;
    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;
    private final RequestClient requestClient;

    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto eventDto) {
        validateEventDate(eventDto.getEventDate());
        CategoryDto category = categoryService.findById(eventDto.getCategory());
        Location location = saveLocation(eventDto.getLocation());
        findUserById(userId);

        final Event event = eventDtoMapper.mapFromDto(eventDto);
        event.setInitiatorId(userId);
        event.setCategory(categoryDtoMapper.mapFromDto(category));
        event.setLocation(location);
        final Event createdEvent = eventRepository.save(event);
        return eventDtoMapper.mapToFullDto(createdEvent);
    }

    @Override
    public Collection<EventShortDto> findAllByPublic(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                     String rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                     Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && LocalDateTime.parse(rangeStart, formatter)
                .isAfter(LocalDateTime.parse(rangeEnd, formatter))) {
            throw new IncorrectRequestException("RangeStart is after Range End");
        }
        if (sort != null && !sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new IncorrectRequestException("Unknown sort type");
        }

        final Collection<Event> events = eventRepository.findAllByPublic(text, categories, paid,
                rangeStart == null ? null : LocalDateTime.parse(rangeStart, formatter),
                rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, formatter), onlyAvailable,
                (Pageable) PageRequest.of(from, size));

        Map<Long, Double> ratings = getRatings(events.stream().map(Event::getId).toList());

        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    EventShortDto dto = eventDtoMapper.mapToShortDto(event);
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .collect(Collectors.toList());

        dtos.sort((e1, e2) -> {
            if (sort == null || sort.equals("EVENT_DATE")) {
                return e1.getEventDate().compareTo(e2.getEventDate());
            } else {
                return Double.compare(e2.getRating(), e1.getRating());
            }
        });

        return dtos;
    }

    @Override
    public Collection<EventShortDto> findAllByPrivate(Long userId, Integer from, Integer size) {
        final Collection<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from, size));
        Map<Long, Double> ratings = getRatings(events.stream().map(Event::getId).toList());
        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventDtoMapper.mapToShortDto(event);
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<EventFullDto> findAllByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                                   String rangeStart, String rangeEnd, Integer from, Integer size) {
        final Collection<Event> events = eventRepository.findAllByAdmin(users, states, categories,
                rangeStart == null ? null : LocalDateTime.parse(rangeStart, formatter),
                rangeEnd == null ? null : LocalDateTime.parse(rangeEnd, formatter), (Pageable) PageRequest.of(from, size));

        Map<Long, Double> ratings = getRatings(events.stream().map(Event::getId).toList());

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventDtoMapper.mapToFullDto(event);
                    dto.setRating(ratings.getOrDefault(event.getId(), 0.0));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findById(Long userId, Long eventId, Boolean isPublic, HttpServletRequest request) {
        final Event event = findEventById(eventId);

        if (isPublic && !event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        } else if (isPublic) {
            if (userId != null) {
                collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
            }
        } else if (userId != null) {
            findUserById(userId);
        }

        final EventFullDto eventDto = eventDtoMapper.mapToFullDto(event);
        Map<Long, Double> ratings = getRatings(List.of(eventId));
        eventDto.setRating(ratings.getOrDefault(eventId, 0.0));
        return eventDto;
    }

    @Override
    public EventFullDto updateByPrivate(Long userId, Long eventId, UpdateEventUserRequest eventDto) {
        final Event event = findEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new ValidationException("User is not the initiator of the event");
        }

        validateUser(event.getInitiatorId(), userId);
        validateEventDate(eventDto.getEventDate());
        validateStatusForPrivate(event.getState(), eventDto.getStateAction());

        final Category category = findCategoryById(eventDto.getCategory());
        final Location location = saveLocation(eventDto.getLocation());
        eventDtoMapper.updateFromDto(event, eventDto);

        Collection<ParticipationRequestDto> confirmedRequests = requestClient.getByEventId(event.getInitiatorId(), eventId);
        event.setConfirmedRequests((long) confirmedRequests.size());

        final Event updatedEvent = eventRepository.save(event);

        final EventFullDto updatedEventDto = eventDtoMapper.mapToFullDto(updatedEvent);

        Map<Long, Double> ratings = getRatings(List.of(eventId));
        updatedEventDto.setRating(ratings.getOrDefault(eventId, 0.0));

        return updatedEventDto;
    }

    @Override
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest eventDto) {
        final Event event = findEventById(eventId);

        validateEventDateForAdmin(eventDto.getEventDate() == null ? event.getEventDate() : LocalDateTime.parse(eventDto
                .getEventDate(), formatter), eventDto.getStateAction());
        validateStatusForAdmin(event.getState(), eventDto.getStateAction());

        final Category category = findCategoryById(eventDto.getCategory());
        final Location location = saveLocation(eventDto.getLocation());
        eventDtoMapper.updateFromDto(event, eventDto);
        if (eventDto.getStateAction() != null && eventDto.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            event.setPublishedOn(LocalDateTime.now());
        }

        Collection<ParticipationRequestDto> confirmedRequests = requestClient.getByEventId(event.getInitiatorId(), eventId);
        event.setConfirmedRequests((long) confirmedRequests.size());

        final Event updatedEvent = eventRepository.save(event);

        final EventFullDto updatedEventDto = eventDtoMapper.mapToFullDto(updatedEvent);

        Map<Long, Double> ratings = getRatings(List.of(eventId));
        updatedEventDto.setRating(ratings.getOrDefault(eventId, 0.0));

        return updatedEventDto;
    }

    @Override
    public void updateEventConfirmedRequests(Long eventId, Long confirmedRequests) {
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " нет в базе данных"));
        event.setConfirmedRequests(confirmedRequests);
        eventRepository.save(event);
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " нет в базе данных"));

        Map<Long, Double> ratings = getRatings(List.of(eventId));
        EventFullDto dto = eventDtoMapper.mapToFullDto(event);
        dto.setRating(ratings.getOrDefault(eventId, 0.0));
        return dto;
    }

    private void validateUser(Long userId, Long initiatorId) {
        if (!initiatorId.equals(userId)) {
            throw new NotFoundException("Trying to change information not from initiator of event");
        }
    }

    private void validateEventDate(String eventDate) {
        if (eventDate != null && LocalDateTime.parse(eventDate, formatter).isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Event date should be early than 2 hours than current moment "
                    + eventDate + " " + LocalDateTime.parse(eventDate, formatter));
        }
    }

    private void validateEventDateForAdmin(LocalDateTime eventDate, StateAction stateAction) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Event date should be early than 2 hours than current moment");
        }
        if (stateAction != null && stateAction.equals(StateAction.PUBLISH_EVENT) && eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ForbiddenException("Event date should be early than 1 hours than publish moment");
        }
    }

    private void validateStatusForPrivate(State state, StateAction stateAction) {
        if (state.equals(State.PUBLISHED)) {
            throw new ConflictException("Can't change event not cancelled or in moderation");
        }
        switch (stateAction) {
            case null:
            case StateAction.CANCEL_REVIEW:
            case StateAction.SEND_TO_REVIEW:
                return;
            default:
                throw new ForbiddenException("Unknown state action");
        }
    }

    private void validateStatusForAdmin(State state, StateAction stateAction) {
        if (!state.equals(State.PENDING) && stateAction.equals(StateAction.PUBLISH_EVENT)) {
            throw new ConflictException("Can't publish not pending event");
        }
        if (state.equals(State.PUBLISHED) && stateAction.equals(StateAction.REJECT_EVENT)) {
            throw new ConflictException("Can't reject already published event");
        }
        if (stateAction != null && !stateAction.equals(StateAction.REJECT_EVENT) && !stateAction
                 .equals(StateAction.PUBLISH_EVENT)) {
            throw new ForbiddenException("Unknown state action");
        }
    }

    private UserDto findUserById(Long userId) {
        final UserDto userDto = userClient.findById(userId);
        return userDto;
    }

    private Category findCategoryById(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        final CategoryDto categoryDto = categoryService.findById(categoryId);
        final Category category = categoryDtoMapper.mapFromDto(categoryDto);
        return category;
    }

    private Location saveLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }
        final LocationDto createdLocationDto = locationService.create(locationDto);
        final Location location = locationDtoMapper.mapFromDto(createdLocationDto);
        return location;
    }

    private Event findEventById(Long eventId) {
        final Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " was not found")
        );

        return event;
    }

    @Override
    public Collection<EventShortDto> getRecommendedEvents(Long userId, Integer maxResults) {
        List<RecommendedEventProto> protos = recommendationsClient.getRecommendationsForUser(userId, maxResults)
                .collect(Collectors.toList());

        List<Long> eventIds = protos.stream()
                .map(RecommendedEventProto::getEventId)
                .collect(Collectors.toList());

        Map<Long, Event> eventsMap = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        return protos.stream()
                .map(proto -> {
                    Event event = eventsMap.get(proto.getEventId());
                    if (event == null) return null;
                    EventShortDto dto = eventDtoMapper.mapToShortDto(event);
                    dto.setRating(proto.getScore());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<EventShortDto> getSimilarEvents(Long userId, Long eventId, Integer maxResults) {
        List<RecommendedEventProto> protos = recommendationsClient.getSimilarEvents(eventId, userId, maxResults)
                .collect(Collectors.toList());

        List<Long> eventIds = protos.stream()
                .map(RecommendedEventProto::getEventId)
                .collect(Collectors.toList());

        Map<Long, Event> eventsMap = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        return protos.stream()
                .map(proto -> {
                    Event event = eventsMap.get(proto.getEventId());
                    if (event == null) return null;
                    EventShortDto dto = eventDtoMapper.mapToShortDto(event);
                    dto.setRating(proto.getScore());
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Double getRating(Long eventId) {
        return recommendationsClient.getInteractionsCount(List.of(eventId))
                .findFirst()
                .map(RecommendedEventProto::getScore)
                .orElse(0.0);
    }

    private Map<Long, Double> getRatings(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return recommendationsClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore,
                        (a, b) -> b
                ));
    }

    @Override
    public void addLikeToEvent(Long eventId, Long userId) {
        if (!requestClient.isUserParticipatedInEvent(eventId, userId)) {
            throw new IncorrectRequestException("Пользователь не участвует в этом событии.");
        }
        collectorClient.sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }
}
