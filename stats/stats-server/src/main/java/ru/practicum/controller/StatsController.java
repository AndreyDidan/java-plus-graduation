package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.CreateHitDto;
import ru.practicum.ResponseHitDto;
import ru.practicum.ResponseStatsDto;
import ru.practicum.service.StatisticService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatisticService statisticService;

    @GetMapping("/stats")
    public List<ResponseStatsDto> getStats(@RequestParam String start,
                                           @RequestParam String end,
                                           @RequestParam(required = false, defaultValue = "") List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("Получен запрос на получение статистики по посещениям");
        return statisticService.get(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseHitDto createHit(@RequestBody @Valid CreateHitDto createHitDto) {
        log.info("Сохранение информации об обращении к эндпоинту");
        return statisticService.create(createHitDto);
    }
}