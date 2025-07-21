package ru.practicum.feign.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.model.EventFullDto;

@FeignClient(name = "event-service", path = "/admin/events")
public interface EventClient {
    @GetMapping("/{eventId}")
    EventFullDto findById(@PathVariable Long eventId) throws FeignException;

    @PutMapping("/request/{eventId}")
    void updateConfirmedRequests(@PathVariable("eventId") Long eventId, @RequestBody Long confirmedRequests);
}
