package ru.practicum;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "stats-server")
public interface StatsClient {
    @GetMapping("/stats")
    List<ResponseStatsDto> getStats(@RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam(required = false, defaultValue = "") List<String> uris,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique);

    @PostMapping("/hit")
    ResponseHitDto createHit(@RequestBody @Valid CreateHitDto createHitDto);
}