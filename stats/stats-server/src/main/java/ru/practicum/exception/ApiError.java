package ru.practicum.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private final int statusCode;
    private final String messageError;
    private final String message;
    private final String stackTrace;
}