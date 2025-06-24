package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@Slf4j
@RequiredArgsConstructor
public class AdminCommentController {
    public final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        log.info("Пришел DELETE запрос /admin/comments/{}", commentId);
        commentService.delete(null, commentId);
        log.info("Отправлен ответ DELETE /admin/comments/{}", commentId);
    }
}
