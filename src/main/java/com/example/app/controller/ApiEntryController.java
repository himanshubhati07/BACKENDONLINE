package com.example.app.controller;

import com.example.app.dto.ApiEntryRequest;
import com.example.app.dto.ApiEntryResponse;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.PageResponse;
import com.example.app.service.ApiEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
@Tag(name = "APIs", description = "API registry CRUD endpoints")
public class ApiEntryController {

    private final ApiEntryService apiEntryService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a new API")
    public ResponseEntity<ApiResponse<ApiEntryResponse>> createApi(@Valid @RequestBody ApiEntryRequest request) {
        ApiEntryResponse response = apiEntryService.createApi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("API created successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List APIs with pagination and optional name search")
    public ResponseEntity<ApiResponse<PageResponse<ApiEntryResponse>>> getApis(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortOrder = Sort.by(direction, sortParts[0]);

        PageResponse<ApiEntryResponse> response = apiEntryService.getApis(
                search, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success("APIs fetched successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get an API by id")
    public ResponseEntity<ApiResponse<ApiEntryResponse>> getApiById(@PathVariable String id) {
        ApiEntryResponse response = apiEntryService.getApiById(id);
        return ResponseEntity.ok(ApiResponse.success("API fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an API by id")
    public ResponseEntity<ApiResponse<ApiEntryResponse>> updateApi(
            @PathVariable String id, @Valid @RequestBody ApiEntryRequest request) {
        ApiEntryResponse response = apiEntryService.updateApi(id, request);
        return ResponseEntity.ok(ApiResponse.success("API updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete an API by id")
    public ResponseEntity<ApiResponse<Object>> deleteApi(@PathVariable String id) {
        apiEntryService.deleteApi(id);
        return ResponseEntity.ok(ApiResponse.success("API deleted successfully", null));
    }
}
