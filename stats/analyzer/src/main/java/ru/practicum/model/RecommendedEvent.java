package ru.practicum.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendedEvent {
    Long eventId;
    double score;
}