package com.godoy.order.service;

import com.godoy.order.dto.request.ProductRequest;
import com.godoy.order.dto.request.UpdateStockRequest;
import com.godoy.order.dto.response.ProductResponse;
import com.godoy.order.exception.BusinessException;
import com.godoy.order.exception.NotFoundException;
import com.godoy.order.mapper.ProductMapper;
import com.godoy.order.model.entity.Category;
import com.godoy.order.model.entity.Product;
import com.godoy.order.repository.CategoryRepository;
import com.godoy.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));

        if (!category.getActive()) {
            throw new BusinessException("Categoria inativa não pode ter produtos");
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        return productMapper.toResponse(product);
    }

    public List<ProductResponse> findAllByCategoryId(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Categoria não encontrada");
        }

        return productRepository.findAllByCategoryId(categoryId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));

        if (!category.getActive()) {
            throw new BusinessException("Categoria inativa não pode ter produtos");
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    public ProductResponse updateStock(UUID id, UpdateStockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        product.setStockQuantity(request.stockQuantity());
        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
        product.setActive(false);
        productRepository.save(product);
    }

    public Product findProductForOrder(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));
    }
}
