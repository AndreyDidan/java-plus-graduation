package ru.practicum.services.user.action;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionService {
    void handleUserAction(UserActionAvro userActionAvro);
}
