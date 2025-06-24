package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("Пришел POST запрос /admin/users с телом {}", newUserRequest);
        final UserDto userDto = userService.create(newUserRequest);
        log.info("Отправлен ответ POST /admin/users с телом {}", userDto);
        return userDto;
    }

    @GetMapping
    public Collection<UserDto> get(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Пришел GET запрос /admin/users?from={}&size={} по клиентам {}", from, size, ids);
        final Collection<UserDto> users = userService.findAll(ids, from, size);
        log.info("Отправлен ответ GET /admin/users?from={}&size={} с телом {}", from, size, ids);
        return users;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("Пришел DELETE запрос /admin/users/{}", userId);
        userService.delete(userId);
        log.info("Отправлен ответ DELETE /admin/users/{}", userId);
    }
}
