package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;

    // Get loan distribution by type
    public Map<String, Object> getLoanDistribution() {
        List<Loan> loans = loanRepository.findAll();
        Map<String, Long> distribution = new HashMap<>();
        
        for (Loan loan : loans) {
            String type = loan.getLoanType();
            distribution.put(type, distribution.getOrDefault(type, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", distribution.keySet());
        result.put("values", distribution.values());
        return result;
    }

    // Get loan status breakdown
    public Map<String, Object> getStatusBreakdown() {
        List<Loan> loans = loanRepository.findAll();
        Map<String, Long> statusCount = new HashMap<>();
        
        for (Loan loan : loans) {
            String status = loan.getStatus();
            statusCount.put(status, statusCount.getOrDefault(status, 0L) + 1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", statusCount.keySet());
        result.put("values", statusCount.values());
        return result;
    }

    // Get total loan amount by month
    public Map<String, Object> getMonthlyLoanAmount() {
        List<Loan> loans = loanRepository.findAll();
        Map<String, BigDecimal> monthlyAmount = new TreeMap<>();
        
        for (Loan loan : loans) {
            String month = loan.getApplicationDate().getYear() + "-" + 
                          String.format("%02d", loan.getApplicationDate().getMonthValue());
            monthlyAmount.put(month, monthlyAmount.getOrDefault(month, BigDecimal.ZERO)
                                    .add(loan.getLoanAmount()));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("months", monthlyAmount.keySet());
        result.put("amounts", monthlyAmount.values());
        return result;
    }

    // Get collection summary
    public Map<String, Object> getCollectionSummary() {
        List<Loan> loans = loanRepository.findAll();
        BigDecimal totalDisbursed = BigDecimal.ZERO;
        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        
        for (Loan loan : loans) {
            totalDisbursed = totalDisbursed.add(loan.getLoanAmount());
            totalCollected = totalCollected.add(loan.getAmountPaid() != null ? loan.getAmountPaid() : BigDecimal.ZERO);
            totalPending = totalPending.add(loan.getRemainingBalance() != null ? loan.getRemainingBalance() : BigDecimal.ZERO);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalDisbursed", totalDisbursed);
        result.put("totalCollected", totalCollected);
        result.put("totalPending", totalPending);
        result.put("collectionRate", totalDisbursed.compareTo(BigDecimal.ZERO) > 0 
                    ? totalCollected.multiply(BigDecimal.valueOf(100))
                        .divide(totalDisbursed, 2, BigDecimal.ROUND_HALF_UP) 
                    : BigDecimal.ZERO);
        return result;
    }

    // Get recent activities
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Recent loans
        List<Loan> recentLoans = loanRepository.findAll();
        recentLoans.sort((a, b) -> b.getApplicationDate().compareTo(a.getApplicationDate()));
        
        for (int i = 0; i < Math.min(5, recentLoans.size()); i++) {
            Loan loan = recentLoans.get(i);
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "LOAN_APPLIED");
            activity.put("message", "Loan #" + loan.getId() + " applied by " + loan.getCustomer().getFullName());
            activity.put("amount", loan.getLoanAmount());
            activity.put("date", loan.getApplicationDate());
            activities.add(activity);
        }
        
        return activities;
    }
}