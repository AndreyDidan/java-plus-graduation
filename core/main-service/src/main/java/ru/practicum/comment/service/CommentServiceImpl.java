package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentDtoMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.mapper.EventDtoMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.IncorrectRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserDtoMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("commentServiceImpl")
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final EventService eventService;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final CommentDtoMapper commentDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final EventDtoMapper eventDtoMapper;

    @Override
    public CommentDto create(Long userId, NewCommentDto commentDto) {
        final UserDto userDto = userService.findById(userId);
        final User user = userDtoMapper.mapFromDto(userDto);
        final EventFullDto eventDto = eventService.findById(userId, commentDto.getEvent(), false, null);
        final Event event = eventDtoMapper.mapFromDto(eventDto);

        final Comment comment = commentDtoMapper.mapFromDto(commentDto);
        comment.setAuthor(user);
        comment.setEvent(event);
        if (commentDto.getReplyOn() != null) {
            final Comment replyOnComment = findById(commentDto.getReplyOn());
            comment.setReplyOn(replyOnComment);
        }

        final Comment createdComment = commentRepository.save(comment);

        return commentDtoMapper.mapToDto(createdComment);
    }

    @Override
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto commentDto) {
        final UserDto userDto = userService.findById(userId);
        final Comment comment = findById(commentId);
        commentDtoMapper.updateFromDto(comment, commentDto);

        final Comment updatedComment = commentRepository.save(comment);
        return commentDtoMapper.mapToDto(updatedComment);
    }

    @Override
    public Collection<CommentDto> findAllByPrivate(Long userId, Integer from, Integer size) {
        final UserDto userDto = userService.findById(userId);
        final Collection<Comment> comments = commentRepository.findAllByAuthorIdOrderByCreatedOn(userDto.getId(), (Pageable) PageRequest.of(from, size));
        return comments.stream()
                .map(commentDtoMapper::mapToDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<CommentDto> findAllByPublic(Long eventId, Integer from, Integer size) {
        final EventFullDto event = eventService.findById(null, eventId, false, null);
        final Collection<Comment> comments = commentRepository.findAllByEventIdOrderByCreatedOn(event.getId(), (Pageable) PageRequest.of(from, size));
        return comments.stream()
                .filter(comment -> comment.getEvent().getState().equals(State.PUBLISHED))
                .map(commentDtoMapper::mapToDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void delete(Long userId, Long commentId) {
        final Comment comment = findById(commentId);
        if (userId != null) {
            final UserDto userDto = userService.findById(userId);
            if (!userDto.getId().equals(comment.getAuthor().getId())) {
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
