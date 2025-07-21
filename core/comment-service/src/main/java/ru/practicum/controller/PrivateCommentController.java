package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.CommentDto;
import ru.practicum.model.NewCommentDto;
import ru.practicum.model.UpdateCommentDto;
import ru.practicum.service.CommentService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/comments")
@Slf4j
@RequiredArgsConstructor
public class PrivateCommentController {
    public final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> getPrivate(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Пришел GET запрос /users/{}/comments?from={}&size={}", userId, from, size);
        final Collection<CommentDto> comments = commentService.findAllByPrivate(userId, from, size);
        log.info("Отправлен ответ GET /users/{}/comments?from={}&size={} с телом: {}", userId, from, size, comments);
        return comments;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(
            @PathVariable Long userId,
            @RequestBody @Valid NewCommentDto commentDto
            ) {
        log.info("Пришел POST запрос /users/{}/comments с телом {}", userId, commentDto);
        final CommentDto comment = commentService.create(userId, commentDto);
        log.info("Отправлен ответ POST /users/{}/comments с телом: {}", userId, comment);
        return comment;
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentDto commentDto
    ) {
        log.info("Пришел PATCH запрос /users/{}/comments/{} с телом {}", userId, commentId, commentDto);
        final CommentDto comment = commentService.update(userId, commentId, commentDto);
        log.info("Отправлен ответ PATCH /users/{}/comments?eventId={} с телом: {}", userId, commentId, comment);
        return comment;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long commentId
    ) {
        log.info("Пришел DELETE запрос /users/{}/comments/{}", userId, commentId);
        commentService.delete(userId, commentId);
        log.info("Отправлен ответ DELETE /users/{}/comments/{}", userId, commentId);
    }
}
