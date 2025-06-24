package ru.practicum.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.StateAction;
import ru.practicum.location.dto.LocationDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Name should be from 20 to 2000 symbols")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Name should be from 20 to 7000 symbols")
    private String description;

    private String eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Limit of participants can't be negative")
    private Long participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Name should be from 3 to 120 symbols")
    private String title;
}
