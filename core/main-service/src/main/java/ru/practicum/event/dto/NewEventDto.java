package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.location.dto.LocationDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Annotation can't be empty")
    @Size(min = 20, max = 2000, message = "Name should be from 20 to 2000 symbols")
    private String annotation;

    @NotNull(message = "Category can't be empty")
    private Long category;

    @NotBlank(message = "Description can't be empty")
    @Size(min = 20, max = 7000, message = "Name should be from 20 to 7000 symbols")
    private String description;

    @NotBlank(message = "Date of event can't be empty")
    private String eventDate;

    @NotNull(message = "Location can't be empty")
    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero(message = "Limit of participants can't be negative")
    private Long participantLimit = 0L;

    private Boolean requestModeration = true;

    @NotBlank(message = "Title can't be empty")
    @Size(min = 3, max = 120, message = "Name should be from 3 to 120 symbols")
    private String title;
}
