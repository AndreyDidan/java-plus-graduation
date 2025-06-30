package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.Collection;

@RestController
@RequestMapping("/events/{eventId}/comments")
@Slf4j
@RequiredArgsConstructor
public class PublicCommentController {
    public final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> get(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Пришел GET запрос /events/{}/comments?from={}&size={}", eventId, from, size);
        final Collection<CommentDto> comments = commentService.findAllByPublic(eventId, from, size);
        log.info("Отправлен ответ GET /events/{}/comments?from={}&size={} с телом: {}", eventId, from, size, comments);
        return comments;
    }
}
