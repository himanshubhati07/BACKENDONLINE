package com.example.app.service;

import com.example.app.document.ApiEntry;
import com.example.app.dto.ApiEntryRequest;
import com.example.app.dto.ApiEntryResponse;
import com.example.app.dto.PageResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ApiEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ApiEntryService {

    private final ApiEntryRepository apiEntryRepository;

    public ApiEntryResponse createApi(ApiEntryRequest request) {
        Instant now = Instant.now();
        ApiEntry apiEntry = ApiEntry.builder()
                .name(request.getName())
                .endpoint(request.getEndpoint())
                .method(request.getMethod().toUpperCase())
                .description(request.getDescription())
                .createdAt(now)
                .updatedAt(now)
                .build();

        ApiEntry saved = apiEntryRepository.save(apiEntry);
        return toResponse(saved);
    }

    public PageResponse<ApiEntryResponse> getApis(String search, Pageable pageable) {
        Page<ApiEntry> page;
        if (StringUtils.hasText(search)) {
            page = apiEntryRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            page = apiEntryRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    public ApiEntryResponse getApiById(String id) {
        ApiEntry apiEntry = apiEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API not found with id: " + id));
        return toResponse(apiEntry);
    }

    public ApiEntryResponse updateApi(String id, ApiEntryRequest request) {
        ApiEntry apiEntry = apiEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API not found with id: " + id));

        apiEntry.setName(request.getName());
        apiEntry.setEndpoint(request.getEndpoint());
        apiEntry.setMethod(request.getMethod().toUpperCase());
        apiEntry.setDescription(request.getDescription());
        apiEntry.setUpdatedAt(Instant.now());

        ApiEntry saved = apiEntryRepository.save(apiEntry);
        return toResponse(saved);
    }

    public void deleteApi(String id) {
        ApiEntry apiEntry = apiEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API not found with id: " + id));
        apiEntryRepository.delete(apiEntry);
    }

    private ApiEntryResponse toResponse(ApiEntry apiEntry) {
        return ApiEntryResponse.builder()
                .id(apiEntry.getId())
                .name(apiEntry.getName())
                .endpoint(apiEntry.getEndpoint())
                .method(apiEntry.getMethod())
                .description(apiEntry.getDescription())
                .createdAt(apiEntry.getCreatedAt())
                .updatedAt(apiEntry.getUpdatedAt())
                .build();
    }
}
