package com.example.app.service;

import com.example.app.document.Product;
import com.example.app.dto.PageResponse;
import com.example.app.dto.ProductRequest;
import com.example.app.dto.ProductResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        Instant now = Instant.now();
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public PageResponse<ProductResponse> getProducts(String search, String category, Pageable pageable) {
        Page<Product> page;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasCategory = StringUtils.hasText(category);

        if (hasSearch && hasCategory) {
            page = productRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(search, category, pageable);
        } else if (hasSearch) {
            page = productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (hasCategory) {
            page = productRepository.findByCategoryIgnoreCase(category, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return PageResponse.from(page.map(this::toResponse));
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        product.setQuantity(request.getQuantity());
        product.setUpdatedAt(Instant.now());

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .quantity(product.getQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
