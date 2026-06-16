package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.*;
import com.lms.loanmanagementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.lms.loanmanagementsystem.entity.TopUpRequest;

@Service
public class TopUpService {

    @Autowired
    private TopUpRepository topUpRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private EmailService emailService;

    // Request top-up
    public Map<String, Object> requestTopUp(Long customerId, Long loanId, BigDecimal requestedAmount, String purpose) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Customer not found");
            return response;
        }
        
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Loan not found");
            return response;
        }
        
        Customer customer = customerOpt.get();
        Loan existingLoan = loanOpt.get();
        
        // Check eligibility
        BigDecimal remainingBalance = existingLoan.getRemainingBalance() != null ? 
            existingLoan.getRemainingBalance() : existingLoan.getTotalPayable().subtract(existingLoan.getAmountPaid());
        
        BigDecimal maxTopUp = existingLoan.getLoanAmount().multiply(new BigDecimal("0.5")); // Max 50% of original loan
        
        if (requestedAmount.compareTo(maxTopUp) > 0) {
            response.put("success", false);
            response.put("message", "Requested amount exceeds maximum top-up amount of MWK " + maxTopUp);
            return response;
        }
        
        if (existingLoan.getInstallmentsPaid() < existingLoan.getTenureMonths() / 2) {
            response.put("success", false);
            response.put("message", "You must pay at least 50% of your current loan before requesting a top-up");
            return response;
        }
        
        TopUpRequest topUp = new TopUpRequest();
        topUp.setCustomer(customer);
        topUp.setExistingLoan(existingLoan);
        topUp.setRequestedAmount(requestedAmount);
        topUp.setPurpose(purpose);
        topUp.setStatus("PENDING");
        
        topUpRepository.save(topUp);
        
        response.put("success", true);
        response.put("message", "Top-up request submitted successfully");
        response.put("requestId", topUp.getId());
        
        return response;
    }
    
    // Get customer's top-up requests
    public List<TopUpRequest> getCustomerTopUps(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) return List.of();
        return topUpRepository.findByCustomer(customerOpt.get());
    }
    
    // Get all pending requests (for admin)
    public List<TopUpRequest> getPendingRequests() {
        return topUpRepository.findByStatus("PENDING");
    }
    
    // Approve top-up (admin)
    public Map<String, Object> approveTopUp(Long requestId, BigDecimal approvedAmount) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<TopUpRequest> requestOpt = topUpRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Request not found");
            return response;
        }
        
        TopUpRequest request = requestOpt.get();
        request.setStatus("APPROVED");
        request.setApprovedAmount(approvedAmount);
        request.setApprovalDate(LocalDateTime.now());
        topUpRepository.save(request);
        
        // Create new loan for the top-up amount
        Loan newLoan = new Loan();
        newLoan.setCustomer(request.getCustomer());
        newLoan.setLoanAmount(approvedAmount);
        newLoan.setTenureMonths(12);
        newLoan.setInterestRate(new BigDecimal("12.0"));
        newLoan.setLoanType("TOP_UP");
        newLoan.setPurpose("Top-up for Loan #" + request.getExistingLoan().getId());
        newLoan.setStatus("APPROVED");
        newLoan.setApprovalDate(LocalDateTime.now());
        
        // Calculate EMI
        BigDecimal monthlyRate = new BigDecimal("12.0").divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(12);
        BigDecimal numerator = approvedAmount.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        BigDecimal emi = numerator.divide(denominator, 2, java.math.RoundingMode.HALF_UP);
        
        newLoan.setEmiAmount(emi);
        newLoan.setTotalPayable(emi.multiply(BigDecimal.valueOf(12)));
        
        loanRepository.save(newLoan);
        
        // Send email notification
        emailService.sendTopUpApprovalEmail(request, newLoan);
        
        response.put("success", true);
        response.put("message", "Top-up approved and new loan created");
        response.put("newLoanId", newLoan.getId());
        
        return response;
    }
    
    // Reject top-up (admin)
    public Map<String, Object> rejectTopUp(Long requestId, String reason) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<TopUpRequest> requestOpt = topUpRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Request not found");
            return response;
        }
        
        TopUpRequest request = requestOpt.get();
        request.setStatus("REJECTED");
        request.setRejectionReason(reason);
        topUpRepository.save(request);
        
        response.put("success", true);
        response.put("message", "Top-up request rejected");
        
        return response;
    }
}