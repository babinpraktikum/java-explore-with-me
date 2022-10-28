package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.model.dto.CategoryDto;
import ru.practicum.explorewithme.model.dto.NewCategoryDto;
import ru.practicum.explorewithme.model.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDto> getCategories(Integer from, Integer size) throws NotFoundException {
        PageRequest page = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(page).getContent();
        if (categories.isEmpty()) {
            throw new NotFoundException("Categories not found");
        }
        return categories.stream().map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long catId) throws NotFoundException {
        Category category = categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException(String.format("Category catId=%s not found", catId)));
        return categoryMapper.toCategoryDto(category);
    }

    public CategoryDto updateCategory(CategoryDto categoryDto) throws BadRequestException, NotFoundException {
        Category existsCategory = categoryRepository.findById(categoryDto.getId()).orElseThrow(() ->
                new NotFoundException(String.format("Category with id=%s not found", categoryDto.getId()))
        );
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName())
                && !existsCategory.getName().equals(categoryDto.getName())) {
            throw new BadRequestException(String.format("Category with name=%s already exists", categoryDto.getName()));
        }
        Category newCategory = categoryMapper.toCategory(categoryDto);
        Category savedCategory = categoryRepository.save(newCategory);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) throws BadRequestException {
        if (categoryRepository.existsByNameIgnoreCase(newCategoryDto.getName())) {
            throw new BadRequestException(String.format("Category with name=%s already exists", newCategoryDto.getName()));
        }
        Category category = categoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    public void deleteCategory(Long catId) throws BadRequestException, NotFoundException {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException(String.format("Category with id=%s not found", catId));
        }
        if (eventRepository.existsByCategoryId(catId)) {
            throw new BadRequestException(String.format("Exist events with category %s", catId));
        }
        categoryRepository.deleteById(catId);
    }
}
