package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.CreateHitDto;
import ru.practicum.ResponseHitDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HitMapper {

    public static Hit mapToHit(CreateHitDto createHitDto) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return Hit.builder()
                .app(createHitDto.getApp())
                .uri(createHitDto.getUri())
                .ip(createHitDto.getIp())
                .timestamp(LocalDateTime.parse(createHitDto.getTimestamp(), formatter)).build();
    }

    public static ResponseHitDto mapToResponseDto(Hit hit) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return ResponseHitDto.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .url(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp().format(formatter)).build();
    }
}
