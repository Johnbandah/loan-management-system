package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import com.lms.loanmanagementsystem.service.EMICalculatorService;
import com.lms.loanmanagementsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private EMICalculatorService emiCalculator;
    
    @Autowired
    private EmailService emailService;
    
    // Apply for a loan
    @PostMapping("/apply/{customerId}")
    public ResponseEntity<?> applyLoan(@PathVariable Long customerId, @RequestBody Map<String, Object> request) {
        Customer customer = customerRepository.findById(customerId)
            .orElse(null);
        
        if (customer == null) {
            return ResponseEntity.badRequest().body("Customer not found");
        }
        
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(new BigDecimal(request.get("loanAmount").toString()));
        loan.setTenureMonths((Integer) request.get("tenureMonths"));
        loan.setInterestRate(new BigDecimal(request.get("interestRate").toString()));
        loan.setLoanType(request.get("loanType").toString());
        loan.setPurpose(request.get("purpose").toString());
        
        // Calculate EMI
        BigDecimal emi = emiCalculator.calculateEMI(
            loan.getLoanAmount(),
            loan.getInterestRate(),
            loan.getTenureMonths()
        );
        loan.setEmiAmount(emi);
        
        // Calculate total payable
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, loan.getTenureMonths());
        loan.setTotalPayable(totalPayable);
        
        Loan savedLoan = loanRepository.save(loan);
        return new ResponseEntity<>(savedLoan, HttpStatus.CREATED);
    }
    
    // Get all loans
    @GetMapping
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }
    
    // Get loans by customer
    @GetMapping("/customer/{customerId}")
    public List<Loan> getLoansByCustomer(@PathVariable Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }
    
    // Get loan by ID
    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return loanRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Approve loan
    @PutMapping("/{id}/approve")
    public ResponseEntity<Loan> approveLoan(@PathVariable Long id) {
        return loanRepository.findById(id)
            .map(loan -> {
                loan.setStatus("APPROVED");
                loan.setApprovalDate(LocalDateTime.now());
                Loan approvedLoan = loanRepository.save(loan);
                
                // Send email notification
                try {
                    emailService.sendLoanApprovalEmail(approvedLoan);
                    System.out.println("✅ Approval email sent for loan: " + id);
                } catch (Exception e) {
                    System.err.println("❌ Failed to send approval email: " + e.getMessage());
                }
                
                return ResponseEntity.ok(approvedLoan);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Disburse loan (mark as money sent to customer)
    @PutMapping("/{id}/disburse")
    public ResponseEntity<?> disburseLoan(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return loanRepository.findById(id)
            .map(loan -> {
                if (!loan.getStatus().equals("APPROVED")) {
                    return ResponseEntity.badRequest().body("Loan must be approved first");
                }
                
                if (loan.getAmountDisbursed().compareTo(BigDecimal.ZERO) > 0) {
                    return ResponseEntity.badRequest().body("Loan already disbursed");
                }
                
                // Set disbursement details
                loan.setAmountDisbursed(loan.getLoanAmount());
                loan.setDisbursementDate(LocalDateTime.now());
                loan.setDisbursementMethod(request.get("method"));
                loan.setDisbursementReference(request.get("reference"));
                loan.setDisbursedBy(request.get("disbursedBy"));
                loan.setStatus("ACTIVE");
                
                // Update remaining balance
                if (loan.getRemainingBalance() == null) {
                    loan.setRemainingBalance(loan.getTotalPayable());
                }
                
                Loan disbursedLoan = loanRepository.save(loan);
                
                // Send email notification
                try {
                    emailService.sendLoanDisbursementEmail(disbursedLoan);
                    System.out.println("✅ Disbursement email sent for loan: " + id);
                } catch (Exception e) {
                    System.err.println("❌ Failed to send disbursement email: " + e.getMessage());
                }
                
                return ResponseEntity.ok(disbursedLoan);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Reject loan
    @PutMapping("/{id}/reject")
    public ResponseEntity<Loan> rejectLoan(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return loanRepository.findById(id)
            .map(loan -> {
                loan.setStatus("REJECTED");
                loan.setRejectionReason(request.get("reason"));
                return ResponseEntity.ok(loanRepository.save(loan));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Calculate EMI (without applying)
    @GetMapping("/calculate-emi")
    public Map<String, Object> calculateEMI(
            @RequestParam BigDecimal amount,
            @RequestParam int tenure,
            @RequestParam BigDecimal rate) {
        
        BigDecimal emi = emiCalculator.calculateEMI(amount, rate, tenure);
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, tenure);
        BigDecimal totalInterest = totalPayable.subtract(amount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("monthlyEMI", emi);
        response.put("totalPayable", totalPayable);
        response.put("totalInterest", totalInterest);
        response.put("principalAmount", amount);
        response.put("tenureMonths", tenure);
        response.put("interestRate", rate);
        return response;
    }
}