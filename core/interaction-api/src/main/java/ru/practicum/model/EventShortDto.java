package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventShortDto {

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private String eventDate;

    private Long id;

    private Long initiator;

    private Boolean paid;

    private String title;

    private Long views;
}
