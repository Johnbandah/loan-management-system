package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.LoanApplication;
import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.repository.LoanApplicationRepository;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EMICalculatorService emiCalculator;

    // Apply for loan
    public Map<String, Object> applyForLoan(Long customerId, Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Customer not found");
            return response;
        }
        
        Customer customer = customerOpt.get();
        
        // Basic eligibility check
        BigDecimal requestedAmount = new BigDecimal(request.get("requestedAmount").toString());
        BigDecimal monthlyIncome = new BigDecimal(request.get("monthlyIncome").toString());
        BigDecimal maxEligible = monthlyIncome.multiply(new BigDecimal("36")); // 3x annual income
        
        if (requestedAmount.compareTo(maxEligible) > 0) {
            response.put("success", false);
            response.put("message", "Requested amount exceeds maximum eligible amount of MWK " + maxEligible);
            return response;
        }
        
        LoanApplication application = new LoanApplication();
        application.setCustomer(customer);
        application.setRequestedAmount(requestedAmount);
        application.setTenureMonths((Integer) request.get("tenureMonths"));
        application.setLoanType(request.get("loanType").toString());
        application.setPurpose(request.get("purpose").toString());
        application.setMonthlyIncome(monthlyIncome);
        application.setEmploymentType(request.get("employmentType").toString());
        application.setEmployerName(request.get("employerName") != null ? request.get("employerName").toString() : "");
        application.setStatus("PENDING");
        
        LoanApplication saved = loanApplicationRepository.save(application);
        
        // Send confirmation email
        try {
            emailService.sendLoanApplicationConfirmationEmail(saved);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
        
        response.put("success", true);
        response.put("message", "Loan application submitted successfully");
        response.put("applicationId", saved.getId());
        response.put("status", saved.getStatus());
        
        return response;
    }

    // Get customer's applications
    public List<LoanApplication> getCustomerApplications(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) return List.of();
        return loanApplicationRepository.findByCustomer(customerOpt.get());
    }

    // Get all pending applications (for admin)
    public List<LoanApplication> getPendingApplications() {
        return loanApplicationRepository.findByStatus("PENDING");
    }

    // Get all applications (for admin)
    public List<LoanApplication> getAllApplications() {
        return loanApplicationRepository.findAll();
    }

    // Review application (admin)
    public Map<String, Object> reviewApplication(Long applicationId, String status, String rejectionReason, String reviewedBy) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<LoanApplication> appOpt = loanApplicationRepository.findById(applicationId);
        if (appOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Application not found");
            return response;
        }
        
        LoanApplication application = appOpt.get();
        
        if (status.equals("APPROVED")) {
            // Create actual loan from application
            Customer customer = application.getCustomer();
            
            Loan loan = new Loan();
            loan.setCustomer(customer);
            loan.setLoanAmount(application.getRequestedAmount());
            loan.setTenureMonths(application.getTenureMonths());
            loan.setInterestRate(new BigDecimal("12.0"));
            loan.setLoanType(application.getLoanType());
            loan.setPurpose(application.getPurpose());
            loan.setStatus("APPROVED");
            loan.setApprovalDate(LocalDateTime.now());
            
            // Calculate EMI
            BigDecimal emi = emiCalculator.calculateEMI(
                loan.getLoanAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()
            );
            loan.setEmiAmount(emi);
            
            BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, loan.getTenureMonths());
            loan.setTotalPayable(totalPayable);
            
            loanRepository.save(loan);
        }
        
        application.setStatus(status);
        if (rejectionReason != null && !rejectionReason.isEmpty()) {
            application.setRejectionReason(rejectionReason);
        }
        application.setReviewDate(LocalDateTime.now());
        application.setReviewedBy(reviewedBy);
        
        loanApplicationRepository.save(application);
        
        // Send email notification
        try {
            emailService.sendLoanApplicationStatusEmail(application);
        } catch (Exception e) {
            System.err.println("Failed to send status email: " + e.getMessage());
        }
        
        response.put("success", true);
        response.put("message", "Application " + status.toLowerCase());
        
        return response;
    }

    // Get application by ID
    public Optional<LoanApplication> getApplicationById(Long id) {
        return loanApplicationRepository.findById(id);
    }
}