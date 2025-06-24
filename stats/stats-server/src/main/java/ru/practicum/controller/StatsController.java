package ru.practicum.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.CreateHitDto;
import ru.practicum.ResponseHitDto;
import ru.practicum.ResponseStatsDto;
import ru.practicum.service.StatisticService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/")
public class StatsController {

    private final StatisticService statisticService;
    final String JSON_FORMAT_PATTERN_TIME = "yyyy-MM-dd HH:mm:ss";

    @GetMapping("/stats")
    public List<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Получен запрос на получение статистики по посещениям");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_TIME);
        String startFormatted = start.format(formatter);
        String endFormatted = end.format(formatter);

        return statisticService.get(startFormatted, endFormatted, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseHitDto createHit(CreateHitDto createHitDto) {
        log.info("Сохранение информации об обращении к эндпоинту");
        return statisticService.create(createHitDto);
    }
}