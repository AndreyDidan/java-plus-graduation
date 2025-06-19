package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ApiError;
import ru.practicum.exception.InternalServerException;
import ru.practicum.exception.ValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(final InternalServerException e, HttpStatus status) {
        log.info("500 {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        String stackTrace = sw.toString();
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error ....", e.getMessage(), stackTrace);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(final ValidationException e) {
        log.info("400 {}", e.getMessage(), e);

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации данных",
                e.getMessage(),
                stackTrace
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }
}
