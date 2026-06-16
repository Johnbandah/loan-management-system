package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import com.lms.loanmanagementsystem.service.RepaymentService;
import com.lms.loanmanagementsystem.service.EmailService;
import com.lms.loanmanagementsystem.dto.LoanStatementDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    @Autowired
    private RepaymentService repaymentService;
    
    @Autowired
    private EmailService emailService;

    // Get loan statement
    @GetMapping("/statement/{loanId}")
    public ResponseEntity<?> getLoanStatement(@PathVariable Long loanId) {
        LoanStatementDTO statement = repaymentService.generateLoanStatement(loanId);
        if (statement == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(statement);
    }

    // Generate payment schedule for a loan
    @PostMapping("/generate/{loanId}")
    public ResponseEntity<?> generateSchedule(@PathVariable Long loanId) {
        try {
            System.out.println("Generating schedule for loan: " + loanId);
            
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Loan not found with ID: " + loanId));
            }
            
            Loan loan = loanOpt.get();
            System.out.println("Loan found: " + loan.getId() + ", EMI: " + loan.getEmiAmount());
            
            // Check if schedule already exists
            List<Repayment> existing = repaymentRepository.findByLoan(loan);
            System.out.println("Existing repayments count: " + existing.size());
            
            if (!existing.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Schedule already exists for this loan"));
            }
            
            if (loan.getEmiAmount() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMI amount not calculated for this loan"));
            }
            
            List<Repayment> schedule = new ArrayList<>();
            for (int i = 1; i <= loan.getTenureMonths(); i++) {
                Repayment repayment = new Repayment();
                repayment.setLoan(loan);
                repayment.setInstallmentNumber(i);
                repayment.setDueDate(LocalDateTime.now().plusMonths(i));
                repayment.setAmountDue(loan.getEmiAmount());
                repayment.setStatus("PENDING");
                schedule.add(repayment);
            }
            
            List<Repayment> saved = repaymentRepository.saveAll(schedule);
            System.out.println("Saved " + saved.size() + " installments");
            
            return ResponseEntity.ok(Map.of(
                "message", "Payment schedule generated successfully",
                "loanId", loanId,
                "installments", saved.size()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate schedule: " + e.getMessage()));
        }
    }

    // Record a payment
    @PostMapping("/pay")
    public ResponseEntity<?> recordPayment(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== RECORDING PAYMENT ===");
            System.out.println("Request: " + request);
            
            // Extract values
            Long loanId = ((Number) request.get("loanId")).longValue();
            Integer installmentNumber = ((Number) request.get("installmentNumber")).intValue();
            BigDecimal amountPaid = new BigDecimal(request.get("amountPaid").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            String transactionReference = (String) request.get("transactionReference");
            
            System.out.println("Loan ID: " + loanId);
            System.out.println("Installment: " + installmentNumber);
            System.out.println("Amount: " + amountPaid);
            
            // Find loan
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Loan not found with ID: " + loanId));
            }
            Loan loan = loanOpt.get();
            System.out.println("Loan found. Customer: " + loan.getCustomer().getFullName());
            
            // Find repayment installment
            Optional<Repayment> repaymentOpt = repaymentRepository.findByLoanAndInstallmentNumber(loan, installmentNumber);
            if (repaymentOpt.isEmpty()) {
                System.out.println("No repayment found for loan " + loanId + ", installment " + installmentNumber);
                List<Repayment> allRepayments = repaymentRepository.findByLoan(loan);
                System.out.println("Total repayments for this loan: " + allRepayments.size());
                for (Repayment r : allRepayments) {
                    System.out.println(" - Installment " + r.getInstallmentNumber() + ": " + r.getStatus());
                }
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Installment not found. Please generate payment schedule first.",
                    "loanId", loanId,
                    "installmentNumber", installmentNumber,
                    "totalInstallments", allRepayments.size()
                ));
            }
            
            Repayment repayment = repaymentOpt.get();
            System.out.println("Repayment found. Current status: " + repayment.getStatus());
            
            // Check if already paid
            if ("PAID".equals(repayment.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "This installment is already paid"));
            }
            
            // Record payment
            repayment.setAmountPaid(amountPaid);
            repayment.setPaidDate(LocalDateTime.now());
            repayment.setStatus("PAID");
            repayment.setPaymentMethod(paymentMethod);
            repayment.setTransactionReference(transactionReference);
            
            Repayment saved = repaymentRepository.save(repayment);
            System.out.println("Payment saved. New status: " + saved.getStatus());
            
            // Update loan totals
            BigDecimal currentPaid = loan.getAmountPaid() != null ? loan.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal newAmountPaid = currentPaid.add(amountPaid);
            loan.setAmountPaid(newAmountPaid);
            
            int currentInstallments = loan.getInstallmentsPaid() != null ? loan.getInstallmentsPaid() : 0;
            loan.setInstallmentsPaid(currentInstallments + 1);
            
            BigDecimal remaining = loan.getTotalPayable().subtract(newAmountPaid);
            loan.setRemainingBalance(remaining);
            
            // Check if loan is fully paid
            if (loan.getInstallmentsPaid() >= loan.getTenureMonths()) {
                loan.setStatus("CLOSED");
            }
            
            loanRepository.save(loan);
            System.out.println("Loan updated. Paid: " + loan.getAmountPaid() + ", Remaining: " + loan.getRemainingBalance());
            System.out.println("=== PAYMENT RECORDED SUCCESSFULLY ===");
            
            // ==============================================
            // SEND EMAIL CONFIRMATION
            // ==============================================
            try {
                emailService.sendPaymentConfirmationEmail(saved, loan);
                System.out.println("✅ Payment confirmation email sent for loan: " + loanId);
            } catch (Exception e) {
                System.err.println("❌ Failed to send payment email: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Payment recorded successfully",
                "paymentId", saved.getId(),
                "installmentNumber", saved.getInstallmentNumber(),
                "status", saved.getStatus()
            ));
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to record payment: " + e.getMessage()));
        }
    }

    // Get payment history
    @GetMapping("/history/{loanId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long loanId) {
        try {
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            List<Repayment> history = repaymentRepository.findByLoan(loanOpt.get());
            System.out.println("History for loan " + loanId + ": " + history.size() + " records");
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // Get loan summary
    @GetMapping("/summary/{loanId}")
    public ResponseEntity<?> getLoanSummary(@PathVariable Long loanId) {
        try {
            Optional<Loan> loanOpt = loanRepository.findById(loanId);
            if (loanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Loan not found"));
            }
            
            Loan loan = loanOpt.get();
            List<Repayment> repayments = repaymentRepository.findByLoan(loan);
            
            long paidCount = repayments.stream().filter(r -> "PAID".equals(r.getStatus())).count();
            double progressPercent = loan.getTenureMonths() > 0 ? (paidCount * 100.0) / loan.getTenureMonths() : 0;
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("loanId", loan.getId());
            summary.put("customerName", loan.getCustomer().getFullName());
            summary.put("totalLoan", loan.getLoanAmount());
            summary.put("totalPayable", loan.getTotalPayable());
            summary.put("amountPaid", loan.getAmountPaid() != null ? loan.getAmountPaid() : BigDecimal.ZERO);
            summary.put("remainingBalance", loan.getRemainingBalance() != null ? loan.getRemainingBalance() : loan.getTotalPayable());
            summary.put("emiAmount", loan.getEmiAmount());
            summary.put("totalInstallments", loan.getTenureMonths());
            summary.put("paidInstallments", paidCount);
            summary.put("pendingInstallments", loan.getTenureMonths() - paidCount);
            summary.put("status", loan.getStatus());
            summary.put("progressPercent", progressPercent);
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}