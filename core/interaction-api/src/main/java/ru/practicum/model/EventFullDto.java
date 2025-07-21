package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private String createdOn;

    private String publishedOn;

    private String description;

    private String eventDate;

    private Long id;

    private Long initiator;

    private LocationDto location;

    private Boolean paid;

    private Long participantLimit;

    private Boolean requestModeration;

    private State state;

    private String title;

    private Long views;
}
