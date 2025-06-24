package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleConflict(final ForbiddenException e) {
        log.info("403 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "For the requested operation the conditions are not met.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final SQLException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.CONFLICT.value(),
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.info("404 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "The required object was not found.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIncorrectRequestException(final IncorrectRequestException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Incorrectly made request.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final HttpClientErrorException.BadRequest e) {
        log.info("400 {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Incorrectly made request.",
                e.getMessage()
        );
    }
}
