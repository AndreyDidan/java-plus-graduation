package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventDtoMapper;
import ru.practicum.model.CompilationDto;
import ru.practicum.model.NewCompilationDto;

@Mapper(componentModel = "spring", uses = {EventDtoMapper.class})
public interface CompilationMapper {
    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(NewCompilationDto compilationDto);
}
