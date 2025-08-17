package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.service.EventService;
import ru.practicum.model.EventFullDto;
import ru.practicum.model.EventShortDto;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
public class PublicController {
    private final EventService eventService;

    @GetMapping
    public Collection<EventShortDto> get(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        log.info("Пришел GET запрос /events с параметрами: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        final Collection<EventShortDto> events = eventService.findAllByPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
        log.info("Отправлен ответ GET /events с телом: {}", events);
        return events;
    }

    @GetMapping("/{eventId}")
    public EventFullDto findById(@PathVariable Long eventId, HttpServletRequest request, @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("Пришел GET запрос /events/{}", eventId);
        final EventFullDto event = eventService.findById(userId, eventId, true, request);
        log.info("Отправлен ответ GET /events/{} с телом: {}", eventId, event);
        return event;
    }

    @PutMapping("/{eventId}/like")
    public void addLikeToEvent(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Пришел PUT запрос /events/{}/like от пользователя {}", eventId, userId);
        eventService.addLikeToEvent(eventId, userId);
        log.info("Обработан PUT запрос /events/{}/like от пользователя {}", eventId, userId);
    }

    @GetMapping("/recommendations")
    public Collection<EventShortDto> getEventsRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId,
                                                        @RequestParam(defaultValue = "20") int maxResults) {
        log.info("Пришел GET запрос /events/recommendations от пользователя {} с параметром maxResults={}",
                userId, maxResults);
        Collection<EventShortDto> recommendations = eventService.getRecommendedEvents(userId, maxResults);
        log.info("Отправлен ответ GET /events/recommendations пользователю {} с телом: {}",
                userId, recommendations);
        return recommendations;
    }
}
