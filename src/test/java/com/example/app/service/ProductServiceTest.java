package com.example.app.service;

import com.example.app.document.Product;
import com.example.app.dto.PageResponse;
import com.example.app.dto.ProductRequest;
import com.example.app.dto.ProductResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    private ProductRequest sampleRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Phone X");
        request.setDescription("A nice phone");
        request.setPrice(new BigDecimal("499.99"));
        request.setCategory("electronics");
        request.setQuantity(10);
        return request;
    }

    @Test
    void createProduct_validRequest_persistsAndReturnsResponse() {
        ProductRequest request = sampleRequest();
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId("prod-1");
            return p;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo("prod-1");
        assertThat(response.getName()).isEqualTo("Phone X");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("499.99"));
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    void getProductById_existing_returnsResponse() {
        Product product = Product.builder()
                .id("prod-1").name("Phone X").price(new BigDecimal("499.99"))
                .category("electronics").quantity(10)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById("prod-1");

        assertThat(response.getName()).isEqualTo("Phone X");
    }

    @Test
    void getProductById_missing_throwsResourceNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById("missing"));
    }

    @Test
    void updateProduct_existing_updatesFieldsAndTimestamp() {
        Product existing = Product.builder()
                .id("prod-1").name("Old Name").price(new BigDecimal("100.00"))
                .category("misc").quantity(1)
                .createdAt(Instant.now().minusSeconds(3600)).updatedAt(Instant.now().minusSeconds(3600))
                .build();
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest request = sampleRequest();
        ProductResponse response = productService.updateProduct("prod-1", request);

        assertThat(response.getName()).isEqualTo("Phone X");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("499.99"));
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getUpdatedAt()).isAfterOrEqualTo(existing.getCreatedAt());
    }

    @Test
    void updateProduct_missing_throwsResourceNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct("missing", sampleRequest()));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_existing_deletesIt() {
        Product existing = Product.builder().id("prod-1").name("Phone X").build();
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(existing));

        productService.deleteProduct("prod-1");

        verify(productRepository).delete(existing);
    }

    @Test
    void deleteProduct_missing_throwsResourceNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct("missing"));
    }

    @Test
    void getProducts_withSearchAndCategory_usesCombinedRepositoryQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of());
        when(productRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
                eq("phone"), eq("electronics"), eq(pageable))).thenReturn(page);

        PageResponse<ProductResponse> response = productService.getProducts("phone", "electronics", pageable);

        assertThat(response.getContent()).isEmpty();
        verify(productRepository).findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
                "phone", "electronics", pageable);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getProducts_noFilters_usesFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of());
        when(productRepository.findAll(pageable)).thenReturn(page);

        PageResponse<ProductResponse> response = productService.getProducts(null, null, pageable);

        assertThat(response.getContent()).isEmpty();
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getProducts_categoryOnly_usesCategoryQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of());
        when(productRepository.findByCategoryIgnoreCase("electronics", pageable)).thenReturn(page);

        productService.getProducts(null, "electronics", pageable);

        verify(productRepository).findByCategoryIgnoreCase("electronics", pageable);
        verify(productRepository, never()).findAll(any(Pageable.class));
    }
}
