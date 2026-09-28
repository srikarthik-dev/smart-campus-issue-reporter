package com.srikarthik.smartcampus.controller;

import com.srikarthik.smartcampus.dto.DashboardSummaryResponse;
import com.srikarthik.smartcampus.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // GET /api/dashboard/summary — single call for all summary stats
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // GET /api/dashboard/by-category
    @GetMapping("/by-category")
    public ResponseEntity<Map<String, Long>> getByCategory() {
        return ResponseEntity.ok(dashboardService.getByCategory());
    }

    // GET /api/dashboard/by-status
    @GetMapping("/by-status")
    public ResponseEntity<Map<String, Long>> getByStatus() {
        return ResponseEntity.ok(dashboardService.getByStatus());
    }

    // GET /api/dashboard/by-priority
    @GetMapping("/by-priority")
    public ResponseEntity<Map<String, Long>> getByPriority() {
        return ResponseEntity.ok(dashboardService.getByPriority());
    }
}
