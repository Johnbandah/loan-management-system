package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/loan-distribution")
    public ResponseEntity<?> getLoanDistribution() {
        return ResponseEntity.ok(analyticsService.getLoanDistribution());
    }

    @GetMapping("/status-breakdown")
    public ResponseEntity<?> getStatusBreakdown() {
        return ResponseEntity.ok(analyticsService.getStatusBreakdown());
    }

    @GetMapping("/monthly-loans")
    public ResponseEntity<?> getMonthlyLoanAmount() {
        return ResponseEntity.ok(analyticsService.getMonthlyLoanAmount());
    }

    @GetMapping("/collection-summary")
    public ResponseEntity<?> getCollectionSummary() {
        return ResponseEntity.ok(analyticsService.getCollectionSummary());
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        return ResponseEntity.ok(analyticsService.getRecentActivities());
    }
}