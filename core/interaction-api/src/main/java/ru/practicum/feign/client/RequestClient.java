package ru.practicum.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.EventRequestStatusUpdateRequest;
import ru.practicum.model.EventRequestStatusUpdateResult;
import ru.practicum.model.ParticipationRequestDto;

import java.util.Collection;

@FeignClient(name = "request-service")
public interface RequestClient {

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId);

    @GetMapping("/users/{userId}/requests")
    Collection<ParticipationRequestDto> getByRequesterId(@PathVariable Long userId);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    Collection<ParticipationRequestDto> getByEventId(@PathVariable Long userId, @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                @RequestBody EventRequestStatusUpdateRequest requestsToUpdate);
}
