package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {

    @NotNull(message = "Event can't be empty")
    private Long event;

    private Long replyOn;

    @NotBlank(message = "Comment can't be empty")
    @Size(min = 2, max = 2000, message = "Comment should be from 2 to 2000 symbols")
    private String text;
}
