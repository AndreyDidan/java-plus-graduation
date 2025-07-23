package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.model.EventRequest;
import ru.practicum.model.ParticipationRequestDto;

@Mapper(componentModel = "spring")
public interface EventRequestDtoMapper {
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "created", source = "created", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    ParticipationRequestDto mapToResponseDto(EventRequest eventRequest);
}
