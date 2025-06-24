package ru.practicum;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "stats-server", path = "/")
public interface StatsClient {

    final String JSON_FORMAT_PATTERN_TIME = "yyyy-MM-dd HH:mm:ss";

    @GetMapping("/stats")
    List<ResponseStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_TIME) LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_TIME) LocalDateTime end,
                                           @RequestParam(required = false, defaultValue = "") List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique);

    @PostMapping("/hit")
    ResponseHitDto createHit(@RequestBody @Valid CreateHitDto createHitDto);
}
