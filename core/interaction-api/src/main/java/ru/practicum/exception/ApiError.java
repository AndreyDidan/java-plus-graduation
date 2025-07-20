package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiError {
    private final int status;
    private final String reason;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();
}