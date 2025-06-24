package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventDtoMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.storage.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventDtoMapper eventDtoMapper;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        log.info("Добавление подборки {}", newCompilationDto.toString());
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);

        compilation.setPinned(Optional.ofNullable(compilation.getPinned()).orElse(false));

        Set<Long> compilationEventIds = Optional.ofNullable(newCompilationDto.getEvents())
                .orElse(Collections.emptySet());
        List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(compilationEventIds));
        compilation.setEvents(new HashSet<>(events));

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка добавлена: {}", savedCompilation);

        return compilationMapper.toCompilationDto(savedCompilation);

    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = validateCompilation(compId);
        log.info("Обновление подборки c {}, на {}", updateCompilationRequest.toString(), compilation.toString());
        if (updateCompilationRequest.getEvents() != null) {
            Set<Long> eventIds = updateCompilationRequest.getEvents();
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            compilation.setEvents(new HashSet<>(events));
            log.trace("Events = {}", compilation.getEvents());
        }

        compilation.setPinned(Optional.ofNullable(updateCompilationRequest.getPinned()).orElse(false));
        log.trace("Pinned = {}", compilation.getPinned());

        compilation.setTitle(Optional.ofNullable(updateCompilationRequest.getTitle()).orElse(compilation.getTitle()));
        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена: {}", compilation);

        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long id) {
        log.info("Удаление подборки c {}", id);
        validateCompilation(id);
        compilationRepository.deleteById(id);
        log.info("Подборка удалена");
    }

    @Override
    public List<CompilationDto> getAllCompilations(Integer from, Integer size, Boolean pinned) {
        log.info("Получение всех подборок с from={}, size={}, pinned={}", from, size, pinned);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned != null) {
            log.info("Получение всех подборок с pinned: {}", pinned);
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
            log.info("Получены подборки с pinned={}: {}", pinned, compilations);
        } else {
            log.info("Получение всех подборок без фильтрации по pinned");
            compilations = compilationRepository.findAll(pageRequest).getContent();
            log.info("Получены все подборки: {}", compilations);

        }
        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                            .map(eventDtoMapper::mapToShortDto)
                            .collect(Collectors.toList());

                    return compilationMapper.toCompilationDto(compilation);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findCompilationById(Long compId) {
        log.info("Получение подборки с compId={}", compId);
        Compilation compilation = validateCompilation(compId);
        log.info("Подборка найдена: {}", compilation);

        return compilationMapper.toCompilationDto(compilation);
    }

    private Compilation validateCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка с id = " + compId + " не найдена."));
    }
}
