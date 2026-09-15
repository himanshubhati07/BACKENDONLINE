package com.example.app.service;

import com.example.app.dto.ProductDtos;
import com.example.app.entity.Product;
import com.example.app.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  private final ProductRepository products;

  public ProductService(ProductRepository products) {
    this.products = products;
  }

  public ProductDtos.ProductResponse create(ProductDtos.ProductRequest request) {
    Product p = new Product();
    p.setName(request.name());
    p.setPrice(request.price());
    p.setAvailableQuantity(request.availableQuantity());
    return response(products.save(p));
  }

  public Page<ProductDtos.ProductResponse> all(Pageable pageable) {
    return products.findAll(pageable).map(this::response);
  }

  public ProductDtos.ProductResponse one(Long id) {
    return response(product(id));
  }

  public void delete(Long id) {
    products.delete(product(id));
  }

  private Product product(Long id) {
    return products
        .findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Product not found"));
  }

  private ProductDtos.ProductResponse response(Product p) {
    return new ProductDtos.ProductResponse(
        p.getId(), p.getName(), p.getPrice(), p.getAvailableQuantity());
  }
}
