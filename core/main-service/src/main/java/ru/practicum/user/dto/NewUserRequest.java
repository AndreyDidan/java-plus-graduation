package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @Email(message = "Email should have suitable format")
    @NotBlank(message = "Email can't be empty")
    @Size(min = 6, max = 254, message = "Email should be from 6 to 254 symbols")
    private String email;

    @NotBlank(message = "Name can't be empty")
    @Size(min = 2, max = 250, message = "Name should be from 2 to 250 symbols")
    private String name;
}
