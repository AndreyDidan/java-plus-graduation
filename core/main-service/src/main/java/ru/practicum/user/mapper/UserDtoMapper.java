package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

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
