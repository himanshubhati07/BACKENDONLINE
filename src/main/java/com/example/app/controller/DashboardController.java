package com.example.app.controller;

import com.example.app.dto.DashboardResponse;
import com.example.app.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Gym insights and statistics")
public class DashboardController {

  @Autowired private DashboardService dashboardService;

  @GetMapping
  @Operation(summary = "Get dashboard summary statistics")
  public ResponseEntity<DashboardResponse> summary() {
    return ResponseEntity.ok(dashboardService.summary());
  }
}
