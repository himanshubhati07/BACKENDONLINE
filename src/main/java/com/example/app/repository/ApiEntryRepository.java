package com.example.app.repository;

import com.example.app.document.ApiEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiEntryRepository extends MongoRepository<ApiEntry, String> {

    Page<ApiEntry> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
