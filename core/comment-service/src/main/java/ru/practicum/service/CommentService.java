package ru.practicum.service;

import ru.practicum.model.CommentDto;
import ru.practicum.model.NewCommentDto;
import ru.practicum.model.UpdateCommentDto;

import java.util.Collection;

public interface CommentService {

    CommentDto create(Long userId, NewCommentDto commentDto);

    CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto);

    Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size);

    Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size);

    void delete(Long userId, Long commentId);
}
