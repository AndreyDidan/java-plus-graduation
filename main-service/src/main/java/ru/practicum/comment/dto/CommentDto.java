package ru.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {

    private Long id;

    private Long eventId;

    private UserShortDto author;

    private Long replyOn;

    private Collection<CommentDto> replies;

    private String text;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
