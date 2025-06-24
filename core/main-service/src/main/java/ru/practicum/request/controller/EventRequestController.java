package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.EventRequestService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class EventRequestController {
    private final EventRequestService eventRequestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Пришел POST запрос /users/{}/requests?eventId={}", userId, eventId);
        final ParticipationRequestDto request = eventRequestService.create(userId, eventId);
        log.info("Отправлен ответ POST /users/{}/requests?eventId={} с телом: {}", userId, eventId, request);
        return request;
    }

    @GetMapping("/requests")
    public Collection<ParticipationRequestDto> getByRequesterId(@PathVariable Long userId) {
        log.info("Пришел GET запрос /users/{}/requests", userId);
        final Collection<ParticipationRequestDto> requests = eventRequestService.getByRequesterId(userId);
        log.info("Отправлен ответ GET /users/{}/requests с телом: {}", userId, requests);
        return requests;
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Пришел PATCH запрос /users/{}/requests/{}/cancel", userId, requestId);
        final ParticipationRequestDto request = eventRequestService.cancel(userId, requestId);
        log.info("Отправлен ответ PATCH /users/{}/requests/{}/cancel с телом: {}", userId, requestId, request);
        return request;
    }

    @GetMapping("/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> getByEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Пришел GET запрос /users/{}/events/{}/requests", userId, eventId);
        final Collection<ParticipationRequestDto> requests = eventRequestService.getByEventId(userId, eventId);
        log.info("Отправлен ответ GET /users/{}/events/{}/requests с телом: {}", userId, eventId, requests);
        return requests;
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest requestsToUpdate) {
        log.info("Пришел PATCH запрос /users/{}/events/{}/requests с телом {}", userId, eventId, requestsToUpdate);
        final EventRequestStatusUpdateResult result = eventRequestService.updateStatus(userId, eventId, requestsToUpdate);
        log.info("Отправлен ответ PATCH /users/{}/events/{}/requests с телом: {}", userId, eventId, result);
        return result;
    }
}
