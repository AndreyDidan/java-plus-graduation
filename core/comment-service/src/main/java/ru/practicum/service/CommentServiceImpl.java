package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feign.client.EventClient;
import ru.practicum.feign.client.UserClient;
import ru.practicum.mapper.CommentDtoMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CommentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("commentServiceImpl")
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final EventClient eventClient;
    private final UserClient userClient;
    private final CommentRepository commentRepository;
    private final CommentDtoMapper commentDtoMapper;

    @Override
    public CommentDto create(Long userId, NewCommentDto commentDto) {
        final UserDto userDto = userClient.findById(userId);
        final EventFullDto eventDto = eventClient.findById(commentDto.getEvent());

        final Comment comment = commentDtoMapper.mapFromDto(commentDto);
        comment.setAuthorId(userId);
        comment.setEventId(commentDto.getEvent());
        if (commentDto.getReplyOn() != null) {
            final Comment replyOnComment = findById(commentDto.getReplyOn());
            comment.setReplyOn(replyOnComment);
        }

        final Comment createdComment = commentRepository.save(comment);

        return commentDtoMapper.mapToDto(createdComment);
    }

    @Override
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto) {
        final ru.practicum.model.UserDto userDto = userClient.findById(userId);
        final Comment comment = findById(commentId);
        commentDtoMapper.updateFromDto(comment, commentDto);

        final Comment updatedComment = commentRepository.save(comment);
        return commentDtoMapper.mapToDto(updatedComment);
    }

    @Override
    public Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size) {
        final UserDto userDto = userClient.findById(userId);
        final Collection<Comment> comments = commentRepository.findAllByAuthorIdOrderByCreatedOn(userDto.getId(), (Pageable) PageRequest.of(from, size));
        return comments.stream()
                .map(commentDtoMapper::mapToDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size) {
        final EventFullDto event = eventClient.findById(eventId);
        final Collection<Comment> comments = commentRepository.findAllByEventIdOrderByCreatedOn(event.getId(), (Pageable) PageRequest.of(from, size));
        return comments.stream()
                .filter(comment -> eventClient.findById(comment.getEventId()).getState().equals(State.PUBLISHED))
                .map(commentDtoMapper::mapToDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void delete(Long userId, Long commentId) {
        final Comment comment = findById(commentId);
        if (userId != null) {
            final ru.practicum.model.UserDto userDto = userClient.findById(userId);
            if (!userDto.getId().equals(comment.getAuthorId())) {
                throw new IncorrectRequestException("Trying to delete comment not from author");
            }
        }
        commentRepository.delete(comment);
    }

    private Comment findById(Long commentId) {
        final Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Comment with id=" + commentId + " was not found")
        );
        return comment;
    }
}
