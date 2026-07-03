package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.LoanApplication;
import com.lms.loanmanagementsystem.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-applications")
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;

    // Customer applies for loan
    @PostMapping("/apply/{customerId}")
    public ResponseEntity<?> applyForLoan(@PathVariable Long customerId, @RequestBody Map<String, Object> request) {
        Map<String, Object> result = loanApplicationService.applyForLoan(customerId, request);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // Get customer's applications
    @GetMapping("/my-applications/{customerId}")
    public ResponseEntity<?> getMyApplications(@PathVariable Long customerId) {
        return ResponseEntity.ok(loanApplicationService.getCustomerApplications(customerId));
    }

    // Get all pending applications (admin)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingApplications() {
        return ResponseEntity.ok(loanApplicationService.getPendingApplications());
    }

    // Get all applications (admin)
    @GetMapping("/all")
    public ResponseEntity<?> getAllApplications() {
        return ResponseEntity.ok(loanApplicationService.getAllApplications());
    }

    // Review application (admin)
    @PutMapping("/review/{applicationId}")
    public ResponseEntity<?> reviewApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        
        String status = request.get("status");
        String rejectionReason = request.get("rejectionReason");
        String reviewedBy = request.get("reviewedBy");
        
        Map<String, Object> result = loanApplicationService.reviewApplication(
            applicationId, status, rejectionReason, reviewedBy
        );
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}