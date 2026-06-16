package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.MobilePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile-payment")
public class MobilePaymentController {

    @Autowired
    private MobilePaymentService mobilePaymentService;

    @PostMapping("/request")
    public ResponseEntity<?> requestPayment(@RequestBody Map<String, Object> request) {
        Long customerId = ((Number) request.get("customerId")).longValue();
        Long loanId = ((Number) request.get("loanId")).longValue();
        Integer installmentNumber = (Integer) request.get("installmentNumber");
        String mobileNumber = (String) request.get("mobileNumber");
        String provider = (String) request.get("provider");
        
        Map<String, Object> result = mobilePaymentService.requestPayment(
            customerId, loanId, installmentNumber, mobileNumber, provider
        );
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> request) {
        String transactionId = (String) request.get("transactionId");
        boolean success = (Boolean) request.get("success");
        
        Map<String, Object> result = mobilePaymentService.confirmPayment(transactionId, success);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @GetMapping("/history/{customerId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(mobilePaymentService.getPaymentHistory(customerId));
    }
}