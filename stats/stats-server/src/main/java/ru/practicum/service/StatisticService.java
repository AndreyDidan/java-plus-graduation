package ru.practicum.service;

import ru.practicum.CreateHitDto;
import ru.practicum.ResponseHitDto;
import ru.practicum.ResponseStatsDto;

import java.util.List;

public interface StatisticService {
    ResponseHitDto create(CreateHitDto createHitDto);

    List<ResponseStatsDto> get(String start, String end, List<String> uris, Boolean unique);
}