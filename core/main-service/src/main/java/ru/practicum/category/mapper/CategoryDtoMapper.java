package ru.practicum.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryDtoMapper {
    CategoryDto mapToDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category mapFromDto(NewCategoryDto categoryDto);

    Category mapFromDto(CategoryDto categoryDto);
}
