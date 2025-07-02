package ru.practicum.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.model.EventRequestStatus;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    @NotEmpty(message = "Have not requests to update")
    private Collection<Long> requestIds;

    @NotBlank(message = "Have not new status for requests to update")
    private EventRequestStatus status;
}
