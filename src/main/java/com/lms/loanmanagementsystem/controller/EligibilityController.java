package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.EligibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityController {

    @Autowired
    private EligibilityService eligibilityService;

    @GetMapping("/max-loan")
    public ResponseEntity<?> getMaxLoanAmount(
            @RequestParam BigDecimal monthlyIncome,
            @RequestParam String loanType) {
        return ResponseEntity.ok(eligibilityService.calculateMaxLoanAmount(monthlyIncome, loanType));
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkEligibility(@RequestBody Map<String, Object> request) {
        Integer creditScore = (Integer) request.get("creditScore");
        BigDecimal monthlyIncome = new BigDecimal(request.get("monthlyIncome").toString());
        BigDecimal requestedAmount = new BigDecimal(request.get("requestedAmount").toString());
        String loanType = (String) request.get("loanType");
        
        return ResponseEntity.ok(eligibilityService.checkEligibility(creditScore, monthlyIncome, requestedAmount, loanType));
    }

    @GetMapping("/offers")
    public ResponseEntity<?> getLoanOffers(
            @RequestParam BigDecimal loanAmount,
            @RequestParam Integer creditScore) {
        return ResponseEntity.ok(eligibilityService.getLoanOffers(loanAmount, creditScore));
    }

    @GetMapping("/credit-score-info")
    public ResponseEntity<?> getCreditScoreInfo(@RequestParam Integer creditScore) {
        return ResponseEntity.ok(eligibilityService.getCreditScoreInfo(creditScore));
    }
}