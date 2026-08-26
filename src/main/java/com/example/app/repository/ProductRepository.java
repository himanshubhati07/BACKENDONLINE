package com.example.app.repository;

import com.example.app.document.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String name, String category, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);
}
