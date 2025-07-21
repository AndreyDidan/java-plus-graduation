package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.Collection;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Collection<CategoryDto> get(
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false, defaultValue = "10") @Positive Integer size
    ) {
        log.info("Пришел GET запрос /categories?from={}&size={}", from, size);
        final Collection<CategoryDto> categories = categoryService.findAll(from, size);
        log.info("Отправлен ответ GET /categories?from={}&size={} с телом: {}", from, size, categories);
        return categories;
    }

    @GetMapping("/{categoryId}")
    public CategoryDto findById(@PathVariable Long categoryId) {
        log.info("Пришел GET запрос /categories/{}", categoryId);
        final CategoryDto category = categoryService.findById(categoryId);
        log.info("Отправлен ответ GET /categories/{} с телом: {}", categoryId, category);
        return category;
    }
}
