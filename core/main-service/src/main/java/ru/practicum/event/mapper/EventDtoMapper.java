package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.location.mapper.LocationDtoMapper;
import ru.practicum.user.mapper.UserDtoMapper;

@Mapper(componentModel = "spring", uses = {
        CategoryDtoMapper.class,
        UserDtoMapper.class,
        LocationDtoMapper.class
})
public interface EventDtoMapper {

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", source = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", source = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss", ignore = true)
    EventFullDto mapToFullDto(Event event);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    EventShortDto mapToShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", expression = "java(0L)")
    @Mapping(target = "state", expression = "java(ru.practicum.event.model.State.PENDING)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", expression = "java(0L)")
    Event mapFromDto(NewEventDto newEventDto);

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", source = "createdOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "publishedOn", source = "publishedOn", dateFormat = "yyyy-MM-dd HH:mm:ss")
    Event mapFromDto(EventFullDto eventFullDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "state", expression = "java((eventDto.getStateAction() != null && eventDto.getStateAction().equals(ru.practicum.event.model.StateAction.SEND_TO_REVIEW)) ? ru.practicum.event.model.State.PENDING : ru.practicum.event.model.State.CANCELED)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateFromDto(@MappingTarget Event event, UpdateEventUserRequest eventDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "state", expression = "java((eventDto.getStateAction() != null && eventDto.getStateAction().equals(ru.practicum.event.model.StateAction.PUBLISH_EVENT)) ? ru.practicum.event.model.State.PUBLISHED : ru.practicum.event.model.State.CANCELED)")
    @Mapping(target = "eventDate", source = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    void updateFromDto(@MappingTarget Event event, UpdateEventAdminRequest eventDto);
}
