package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.TopUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/topup")
public class TopUpController {

    @Autowired
    private TopUpService topUpService;

    @PostMapping("/request")
    public ResponseEntity<?> requestTopUp(@RequestBody Map<String, Object> request) {
        Long customerId = ((Number) request.get("customerId")).longValue();
        Long loanId = ((Number) request.get("loanId")).longValue();
        BigDecimal requestedAmount = new BigDecimal(request.get("requestedAmount").toString());
        String purpose = (String) request.get("purpose");
        
        Map<String, Object> result = topUpService.requestTopUp(customerId, loanId, requestedAmount, purpose);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/my-requests/{customerId}")
    public ResponseEntity<?> getMyRequests(@PathVariable Long customerId) {
        return ResponseEntity.ok(topUpService.getCustomerTopUps(customerId));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests() {
        return ResponseEntity.ok(topUpService.getPendingRequests());
    }

    @PostMapping("/approve/{requestId}")
    public ResponseEntity<?> approveRequest(@PathVariable Long requestId, @RequestBody Map<String, Object> request) {
        BigDecimal approvedAmount = new BigDecimal(request.get("approvedAmount").toString());
        Map<String, Object> result = topUpService.approveTopUp(requestId, approvedAmount);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        Map<String, Object> result = topUpService.rejectTopUp(requestId, reason);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}