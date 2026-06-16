package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.entity.MobilePayment;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import com.lms.loanmanagementsystem.repository.MobilePaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class MobilePaymentService {

    @Autowired
    private MobilePaymentRepository mobilePaymentRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    @Autowired
    private EmailService emailService;

    // Request mobile money payment
    public Map<String, Object> requestPayment(Long customerId, Long loanId, Integer installmentNumber, 
                                               String mobileNumber, String provider) {
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
        
        Loan loan = loanOpt.get();
        Customer customer = customerOpt.get();
        
        // Check if installment exists and is pending
        Optional<Repayment> repaymentOpt = repaymentRepository.findByLoanAndInstallmentNumber(loan, installmentNumber);
        if (repaymentOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Installment not found");
            return response;
        }
        
        Repayment repayment = repaymentOpt.get();
        if ("PAID".equals(repayment.getStatus())) {
            response.put("success", false);
            response.put("message", "This installment is already paid");
            return response;
        }
        
        // Generate unique transaction ID
        String transactionId = generateTransactionId(provider);
        
        // Create payment request
        MobilePayment payment = new MobilePayment();
        payment.setCustomer(customer);
        payment.setLoan(loan);
        payment.setInstallmentNumber(installmentNumber);
        payment.setAmount(repayment.getAmountDue());
        payment.setMobileNumber(mobileNumber);
        payment.setProvider(provider);
        payment.setTransactionId(transactionId);
        payment.setStatus("PENDING");
        
        mobilePaymentRepository.save(payment);
        
        // Simulate payment processing (in real implementation, this would call mobile money API)
        response.put("success", true);
        response.put("message", "Payment request initiated. Please check your phone for confirmation.");
        response.put("transactionId", transactionId);
        response.put("amount", repayment.getAmountDue());
        response.put("provider", provider);
        
        return response;
    }
    
    // Simulate payment confirmation (webhook would handle this in production)
    public Map<String, Object> confirmPayment(String transactionId, boolean success) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<MobilePayment> paymentOpt = mobilePaymentRepository.findById(
            mobilePaymentRepository.findAll().stream()
                .filter(p -> p.getTransactionId().equals(transactionId))
                .findFirst()
                .map(MobilePayment::getId)
                .orElse(null)
        );
        
        if (paymentOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Payment not found");
            return response;
        }
        
        MobilePayment payment = paymentOpt.get();
        
        if (success) {
            payment.setStatus("SUCCESS");
            payment.setCompletedAt(LocalDateTime.now());
            payment.setResponseMessage("Payment successful");
            mobilePaymentRepository.save(payment);
            
            // Record the payment in the repayment system
            recordPaymentFromMobile(payment);
            
            response.put("success", true);
            response.put("message", "Payment confirmed successfully!");
        } else {
            payment.setStatus("FAILED");
            payment.setResponseMessage("Payment failed");
            mobilePaymentRepository.save(payment);
            
            response.put("success", false);
            response.put("message", "Payment failed. Please try again.");
        }
        
        return response;
    }
    
    // Record payment from mobile money
    private void recordPaymentFromMobile(MobilePayment payment) {
        Optional<Loan> loanOpt = loanRepository.findById(payment.getLoan().getId());
        if (loanOpt.isEmpty()) return;
        
        Loan loan = loanOpt.get();
        
        Optional<Repayment> repaymentOpt = repaymentRepository.findByLoanAndInstallmentNumber(loan, payment.getInstallmentNumber());
        if (repaymentOpt.isEmpty()) return;
        
        Repayment repayment = repaymentOpt.get();
        
        if ("PAID".equals(repayment.getStatus())) return;
        
        // Record payment
        repayment.setAmountPaid(payment.getAmount());
        repayment.setPaidDate(LocalDateTime.now());
        repayment.setStatus("PAID");
        repayment.setPaymentMethod(payment.getProvider());
        repayment.setTransactionReference(payment.getTransactionId());
        repaymentRepository.save(repayment);
        
        // Update loan totals
        BigDecimal currentPaid = loan.getAmountPaid() != null ? loan.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newAmountPaid = currentPaid.add(payment.getAmount());
        loan.setAmountPaid(newAmountPaid);
        
        int currentInstallments = loan.getInstallmentsPaid() != null ? loan.getInstallmentsPaid() : 0;
        loan.setInstallmentsPaid(currentInstallments + 1);
        
        BigDecimal remaining = loan.getTotalPayable().subtract(newAmountPaid);
        loan.setRemainingBalance(remaining);
        
        if (loan.getInstallmentsPaid() >= loan.getTenureMonths()) {
            loan.setStatus("CLOSED");
        }
        
        loanRepository.save(loan);
        
        // Send email confirmation
        emailService.sendPaymentConfirmationEmail(repayment, loan);
    }
    
    // Get payment history for customer
    public List<MobilePayment> getPaymentHistory(Long customerId) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) return new java.util.ArrayList<>();
        return mobilePaymentRepository.findByCustomer(customerOpt.get());
    }
    
    // Generate unique transaction ID
    private String generateTransactionId(String provider) {
        String prefix = provider.equals("AIRTEL_MONEY") ? "AM" : "TM";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }
}