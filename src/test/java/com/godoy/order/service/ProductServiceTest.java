package com.godoy.order.service;

import com.godoy.order.dto.request.ProductRequest;
import com.godoy.order.dto.request.UpdateStockRequest;
import com.godoy.order.dto.response.CategoryResponse;
import com.godoy.order.dto.response.ProductResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.CategoryMapper;
import com.godoy.order.mapper.ProductMapper;
import com.godoy.order.model.entity.Category;
import com.godoy.order.model.entity.Product;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private ProductRequest request;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Cell Phones")
                .description("Smartphones and accessories")
                .build();

        request = new ProductRequest(
                "Apple iPhone 17 Pro Max",
                "iPhone 17 Pro Max (256GB) Azul-intenso",
                new BigDecimal("10000.00"),
                10,
                category.getId()
        );

        product = Product.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(category)
                .active(true)
                .build();

        response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                category.getName(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar produto com sucesso")
        void shouldCreateProductSuccessfully() {
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
            when(productMapper.toEntity(request)).thenReturn(product);
            when(productRepository.save(any())).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Apple iPhone 17 Pro Max");
            assertThat(result.price()).isEqualByComparingTo("10000.00");
            assertThat(result.stockQuantity()).isEqualTo(10);
            verify(productRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando categoria não encontrada")
        void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando categoria inativa")
        void shouldThrowBusinessExceptionWhenCategoryInactive() {
            category.setActive(false);
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> productService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria inativa não pode ter produtos");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de produtos")
        void shouldReturnListOfProducts() {
            when(productRepository.findAll()).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);

            List<ProductResponse> result = productService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Apple iPhone 17 Pro Max");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há produtos")
        void shouldReturnEmptyListWhenNoProducts() {
            when(productRepository.findAll()).thenReturn(List.of());

            List<ProductResponse> result = productService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar produto por ID com sucesso")
        void shouldFindProductByIdSuccessfully() {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.findById(product.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(product.getId());
            assertThat(result.categoryName()).isEqualTo("Cell Phones");
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando produto não encontrado")
        void shouldThrowNotFoundWhenProductNotFound() {
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Produto não encontrado");
        }
    }

    @Nested
    @DisplayName("findAllByCategoryId")
    class FindAllByCategoryId {

        @Test
        @DisplayName("Deve retornar produtos da categoria")
        void shouldReturnProductByCategoryId() {
            when(categoryRepository.existsById(category.getId())).thenReturn(true);
            when(productRepository.findAllByCategoryId(category.getId())).thenReturn(List.of(product));
            when(productMapper.toResponse(product)).thenReturn(response);

            List<ProductResponse> result = productService.findAllByCategoryId(category.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando categoria não encontrada")
        void shouldThrowNotFoundExceptionWhenCategoryNotFound() {
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar produto com sucesso")
        void shouldUpdateProductSuccessfully() {
            product.setName("Apple iPhone 17 Pro");
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));
            when(productRepository.save(any())).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.update(product.getId(), request);

            assertThat(result).isNotNull();
            verify(productRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar produto inexistente")
        void shouldThrowNotFoundWhenUpdatingNonExistentProduct() {
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(UUID.randomUUID(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Produto não encontrado");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar com categoria inexistente")
        void shouldThrowNotFoundWhenCategoryNotFoundOnUpdate() {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(product.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Categoria não encontrada");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com categoria inativa")
        void shouldThrowBusinessExceptionWhenCategoryInactiveOnUpdate() {
            category.setActive(false);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(categoryRepository.findById(request.categoryId())).thenReturn(Optional.of(category));

            assertThatThrownBy(() -> productService.update(product.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Categoria inativa não pode ter produtos");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateStock")
    class UpdateStock {

        @Test
        @DisplayName("Deve atualizar estoque com sucesso")
        void shouldUpdateStockSuccessfully() {
            UpdateStockRequest stockRequest = new UpdateStockRequest(20);
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);
            when(productMapper.toResponse(product)).thenReturn(response);

            ProductResponse result = productService.updateStock(product.getId(), stockRequest);

            assertThat(result).isNotNull();
            verify(productRepository).save(argThat(p -> p.getStockQuantity().equals(20)));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar estoque de produto inexistente")
        void shouldThrowBusinessExceptionWhenCategoryInactiveOnUpdate() {
            UpdateStockRequest stockRequest = new UpdateStockRequest(20);
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateStock(product.getId(), stockRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Produto não encontrado");

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar produto com sucesso")
        void shouldDeleteProductSuccessfully() {
            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            productService.delete(product.getId());

            verify(productRepository).save(argThat(p -> !p.getActive()));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar produto inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentProduct() {
            UpdateStockRequest stockRequest = new UpdateStockRequest(20);
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateStock(product.getId(), stockRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Produto não encontrado");

            verify(productRepository, never()).save(any());
        }
    }
}