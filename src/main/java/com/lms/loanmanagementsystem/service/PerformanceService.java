package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    @Autowired
    private CustomerRepository customerRepository;

    // Get portfolio summary
    public Map<String, Object> getPortfolioSummary() {
        List<Loan> loans = loanRepository.findAll();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLoans", loans.size());
        summary.put("totalAmount", loans.stream().mapToDouble(l -> l.getLoanAmount().doubleValue()).sum());
        
        long activeLoans = loans.stream().filter(l -> l.getStatus().equals("APPROVED") || l.getStatus().equals("ACTIVE")).count();
        long closedLoans = loans.stream().filter(l -> l.getStatus().equals("CLOSED")).count();
        long pendingLoans = loans.stream().filter(l -> l.getStatus().equals("PENDING")).count();
        
        summary.put("activeLoans", activeLoans);
        summary.put("closedLoans", closedLoans);
        summary.put("pendingLoans", pendingLoans);
        
        BigDecimal totalCollected = loans.stream()
            .map(l -> l.getAmountPaid() != null ? l.getAmountPaid() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalReceivable = loans.stream()
            .map(l -> l.getTotalPayable() != null ? l.getTotalPayable() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.put("totalCollected", totalCollected);
        summary.put("totalReceivable", totalReceivable);
        
        if (totalReceivable.compareTo(BigDecimal.ZERO) > 0) {
            double collectionRate = totalCollected.doubleValue() / totalReceivable.doubleValue() * 100;
            summary.put("collectionRate", Math.round(collectionRate * 100.0) / 100.0);
        } else {
            summary.put("collectionRate", 0.0);
        }
        
        return summary;
    }

    // Get monthly collection trend
    public Map<String, Object> getMonthlyCollectionTrend() {
        List<Repayment> repayments = repaymentRepository.findAll();
        Map<String, Double> monthlyData = new LinkedHashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        
        LocalDateTime startDate = LocalDateTime.now().minusMonths(11);
        LocalDateTime endDate = LocalDateTime.now();
        
        // Initialize all months with 0
        for (int i = 0; i < 12; i++) {
            String month = startDate.plusMonths(i).format(formatter);
            monthlyData.put(month, 0.0);
        }
        
        // Add actual collection data
        for (Repayment repayment : repayments) {
            if (repayment.getPaidDate() != null && repayment.getStatus().equals("PAID")) {
                String month = repayment.getPaidDate().format(formatter);
                if (monthlyData.containsKey(month)) {
                    monthlyData.put(month, monthlyData.get(month) + repayment.getAmountPaid().doubleValue());
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", monthlyData.keySet());
        result.put("data", monthlyData.values());
        
        return result;
    }

    // Get loan distribution by type
    public Map<String, Object> getLoanTypeDistribution() {
        List<Loan> loans = loanRepository.findAll();
        Map<String, Long> distribution = loans.stream()
            .collect(Collectors.groupingBy(l -> l.getLoanType() != null ? l.getLoanType() : "OTHER", Collectors.counting()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("labels", distribution.keySet());
        result.put("data", distribution.values());
        
        return result;
    }

    // Get top customers
    public List<Map<String, Object>> getTopCustomers() {
        List<Loan> loans = loanRepository.findAll();
        Map<Long, Map<String, Object>> customerMap = new HashMap<>();
        
        for (Loan loan : loans) {
            Long customerId = loan.getCustomer().getId();
            if (!customerMap.containsKey(customerId)) {
                Map<String, Object> customerData = new HashMap<>();
                customerData.put("name", loan.getCustomer().getFullName());
                customerData.put("email", loan.getCustomer().getEmail());
                customerData.put("totalLoans", 0L);
                customerData.put("totalAmount", 0.0);
                customerData.put("totalPaid", 0.0);
                customerData.put("loans", new ArrayList<Map<String, Object>>());
                customerMap.put(customerId, customerData);
            }
            
            Map<String, Object> customerData = customerMap.get(customerId);
            customerData.put("totalLoans", ((Long) customerData.get("totalLoans")) + 1);
            customerData.put("totalAmount", ((Double) customerData.get("totalAmount")) + loan.getLoanAmount().doubleValue());
            customerData.put("totalPaid", ((Double) customerData.get("totalPaid")) + (loan.getAmountPaid() != null ? loan.getAmountPaid().doubleValue() : 0.0));
            
            Map<String, Object> loanInfo = new HashMap<>();
            loanInfo.put("id", loan.getId());
            loanInfo.put("amount", loan.getLoanAmount().doubleValue());
            loanInfo.put("status", loan.getStatus());
            loanInfo.put("paid", loan.getAmountPaid() != null ? loan.getAmountPaid().doubleValue() : 0.0);
            ((List<Map<String, Object>>) customerData.get("loans")).add(loanInfo);
        }
        
        // Sort by total amount
        List<Map<String, Object>> sortedCustomers = new ArrayList<>(customerMap.values());
        sortedCustomers.sort((a, b) -> ((Double) b.get("totalAmount")).compareTo((Double) a.get("totalAmount")));
        
        return sortedCustomers.stream().limit(10).collect(Collectors.toList());
    }

    // Get performance metrics
    public Map<String, Object> getPerformanceMetrics() {
        List<Loan> loans = loanRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Average loan size
        double avgLoanSize = loans.stream()
            .mapToDouble(l -> l.getLoanAmount().doubleValue())
            .average()
            .orElse(0.0);
        metrics.put("averageLoanSize", avgLoanSize);
        
        // Average interest rate
        double avgInterestRate = loans.stream()
            .mapToDouble(l -> l.getInterestRate().doubleValue())
            .average()
            .orElse(0.0);
        metrics.put("averageInterestRate", avgInterestRate);
        
        // Total customers
        metrics.put("totalCustomers", customers.size());
        
        // Customers with loans
        long customersWithLoans = customers.stream()
            .filter(c -> !loanRepository.findByCustomer(c).isEmpty())
            .count();
        metrics.put("customersWithLoans", customersWithLoans);
        
        // Default rate (loans overdue > 30 days)
        long overdueLoans = 0;
        for (Loan loan : loans) {
            List<Repayment> repayments = repaymentRepository.findByLoan(loan);
            for (Repayment repayment : repayments) {
                if (repayment.getStatus().equals("PENDING") && 
                    repayment.getDueDate().isBefore(LocalDateTime.now().minusDays(30))) {
                    overdueLoans++;
                    break;
                }
            }
        }
        metrics.put("overdueLoans", overdueLoans);
        
        return metrics;
    }
}