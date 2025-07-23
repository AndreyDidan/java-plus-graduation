package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.model.NewUserRequest;
import ru.practicum.model.User;
import ru.practicum.model.UserDto;
import ru.practicum.model.UserShortDto;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    UserDto mapToDto(User user);

    UserShortDto mapToShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User mapFromDto(NewUserRequest newUserRequest);

    User mapFromDto(UserDto userDto);

    @Mapping(target = "email", ignore = true)
    User mapFromDto(UserShortDto userDto);
}
