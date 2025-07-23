package ru.practicum.service;


import ru.practicum.model.NewUserRequest;
import ru.practicum.model.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {

    UserDto create(NewUserRequest newUserRequest);

    Collection<UserDto> findAll(List<Long> ids, Integer from, Integer size);

    UserDto findById(Long userId);

    void delete(Long userId);
}
