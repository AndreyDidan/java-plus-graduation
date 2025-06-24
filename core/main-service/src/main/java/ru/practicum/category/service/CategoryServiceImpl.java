package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.storage.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service("categoryServiceImpl")
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        final Category category = categoryDtoMapper.mapFromDto(newCategoryDto);
        final Category createdCategory = categoryRepository.save(category);
        return categoryDtoMapper.mapToDto(createdCategory);
    }

    @Override
    public Collection<CategoryDto> findAll(Integer from, Integer size) {
        final Collection<Category> categories = categoryRepository.findAll(PageRequest.of(from, size)).getContent();
        return categories.stream()
                .map(categoryDtoMapper::mapToDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public CategoryDto findById(Long categoryId) {
        final Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Category with id=" + categoryId + " was not found")
        );
        return categoryDtoMapper.mapToDto(category);
    }

    @Override
    public CategoryDto update(Long categoryId, CategoryDto categoryDto) {
        final Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Category with id=" + categoryId + " was not found")
        );
        category.setName(categoryDto.getName());
        final Category updatedCategory = categoryRepository.save(category);
        return categoryDtoMapper.mapToDto(updatedCategory);

    }

    @Override
    public void delete(Long categoryId) {
        final Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Category with id=" + categoryId + " was not found")
        );
        categoryRepository.delete(category);
    }
}
