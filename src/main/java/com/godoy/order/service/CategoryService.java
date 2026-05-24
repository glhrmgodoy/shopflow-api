package com.godoy.order.service;

import com.godoy.order.dto.request.CategoryRequest;
import com.godoy.order.dto.response.CategoryResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.CategoryMapper;
import com.godoy.order.model.entity.Category;
import com.godoy.order.repository.CategoryRepository;
import com.godoy.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Categoria já cadastrada");
        }

        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));

        if (!category.getName().equals(request.name()) &&
                categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Categoria já cadastrada");
        }

        category.setName(request.name());
        category.setDescription(request.description());

        Category update = categoryRepository.save(category);
        return categoryMapper.toResponse(update);
    }

    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));

        if (productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new BusinessException("Categoria possui produtos ativos e não pode ser inativada");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }
}
