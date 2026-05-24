package com.godoy.order.service;

import com.godoy.order.dto.request.CategoryRequest;
import com.godoy.order.dto.response.CategoryResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.CategoryMapper;
import com.godoy.order.model.entity.Category;
import com.godoy.order.repository.CategoryRepository;
import com.godoy.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        request = new CategoryRequest(
                "Eletrônicos",
                "Produtos eletrônicos em geral"
        );

        category = Category.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .description(request.description())
                .active(true)
                .build();

        response = new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar categoria com sucesso")
        void shouldCreateCategorySuccessfully() {
            when(categoryRepository.existsByName(any())).thenReturn(false);
            when(categoryMapper.toEntity(request)).thenReturn(category);
            when(categoryRepository.save(any())).thenReturn(category);
            when(categoryMapper.toResponse(any())).thenReturn(response);

            CategoryResponse result = categoryService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(category.getName());
            verify(categoryRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando categoria já cadastrada")
        void shouldThrowBusinessExceptionWhenCategoryExists() {
            when(categoryRepository.existsByName(request.name())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria já cadastrada");

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de categorias")
        void shouldReturnListOfCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(response);

            List<CategoryResponse> result = categoryService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo(category.getName());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há categorias")
        void shouldReturnEmptyListWhenNoCategories() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponse> result = categoryService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar categoria por ID com sucesso")
        void shouldFindCategoryByIdSuccessfully() {
            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(response);

            CategoryResponse result = categoryService.findById(category.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(category.getId());
            assertThat(result.name()).isEqualTo(category.getName());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando categoria não encontrada")
        void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar categoria com sucesso")
        void shouldUpdateCategorySuccessfully() {
            category.setName("Novo nome");

            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(categoryRepository.save(any())).thenReturn(category);
            when(categoryMapper.toResponse(category)).thenReturn(response);

            CategoryResponse result = categoryService.update(category.getId(), request);

            assertThat(result).isNotNull();
            verify(categoryRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar categoria inexistente")
        void shouldThrowNotFoundWhenUpdatingNonExistentCategory() {
            when(categoryRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(UUID.randomUUID(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com nome já cadastrado")
        void shouldThrowBusinessExceptionWhenNameAlreadyExists() {
            category.setName("Outro nome");

            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(request.name())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.update(category.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria já cadastrada");

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar categoria com sucesso")
        void shouldDeleteCategorySuccessfully() {
            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(productRepository.existsByCategoryIdAndActiveTrue(category.getId())).thenReturn(false);

            categoryService.delete(category.getId());

            verify(categoryRepository).save(argThat(c -> !c.getActive()));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar categoria inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentCategory() {
            when(categoryRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.delete(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao inativar categoria com produtos ativos")
        void shouldThrowBusinessExceptionWhenCategoryHasActiveProducts() {
            when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
            when(productRepository.existsByCategoryIdAndActiveTrue(category.getId())).thenReturn(true);

            assertThatThrownBy(() -> categoryService.delete(category.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria possui produtos ativos e não pode ser inativada");

            verify(categoryRepository, never()).save(any());
        }
    }

}