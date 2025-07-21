package ru.practicum.feign.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.CommentDto;
import ru.practicum.model.NewCommentDto;
import ru.practicum.model.UpdateCommentDto;

import java.util.Collection;

@FeignClient(name = "comment-service")
public interface CommentClient {
    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long commentId);

    @GetMapping("/users/{userId}/comments")
    Collection<CommentDto> getPrivate(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    );

    @PostMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto create(
            @PathVariable Long userId,
            @RequestBody @Valid NewCommentDto commentDto
    );

    @PatchMapping("/users/{userId}/comments/{commentId}")
    CommentDto update(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentDto commentDto
    );

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
            @PathVariable Long userId,
            @PathVariable Long commentId
    );

    @GetMapping("/events/{eventId}/comments")
    Collection<CommentDto> getPublic(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    );
}
