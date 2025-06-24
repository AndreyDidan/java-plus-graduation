package ru.practicum.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.model.EventRequestStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class  ParticipationRequestDto {
    private String created;

    private Long event;

    private Long id;

    private Long requester;

    private EventRequestStatus status;
}
