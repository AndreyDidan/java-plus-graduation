package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.Collection;

public interface CommentService {

    CommentDto create(Long userId, NewCommentDto commentDto);

    CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto);

    Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size);

    Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size);

    void delete(Long userId, Long commentId);
}
