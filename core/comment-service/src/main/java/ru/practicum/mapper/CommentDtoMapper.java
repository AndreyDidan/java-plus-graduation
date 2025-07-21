package ru.practicum.mapper;

import org.mapstruct.*;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentDto;
import ru.practicum.model.NewCommentDto;
import ru.practicum.model.UpdateCommentDto;

@Mapper(componentModel = "spring")
public interface CommentDtoMapper {
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "replyOn", source = "replyOn.id")
    @Mapping(target = "author", source = "authorId")
    CommentDto mapToDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "event")
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "replyOn", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    Comment mapFromDto(NewCommentDto commentDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "replyOn", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", expression = "java(java.time.LocalDateTime.now())")
    Comment updateFromDto(@MappingTarget Comment comment, UpdateCommentDto commentDto);
}
