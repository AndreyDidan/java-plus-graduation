package ru.practicum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStatsDto {

    @NotNull
    private String start;

    @NotNull
    private String end;

    private List<String> uris = new ArrayList<>();

    private Boolean unique;
}
