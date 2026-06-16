package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lms.loanmanagementsystem.dto.LoanStatementDTO;
import java.util.stream.Collectors;

@Service
public class RepaymentService {
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    @Autowired
    private LoanRepository loanRepository;

    // Add this method to RepaymentService class
public LoanStatementDTO generateLoanStatement(Long loanId) {
    Loan loan = loanRepository.findById(loanId).orElse(null);
    if (loan == null) return null;
    
    List<Repayment> repayments = repaymentRepository.findByLoan(loan);
    
    LoanStatementDTO statement = new LoanStatementDTO();
    statement.setLoanId(loan.getId());
    statement.setCustomerName(loan.getCustomer().getFullName());
    statement.setCustomerEmail(loan.getCustomer().getEmail());
    statement.setCustomerPhone(loan.getCustomer().getPhone());
    statement.setLoanAmount(loan.getLoanAmount());
    statement.setInterestRate(loan.getInterestRate());
    statement.setTenureMonths(loan.getTenureMonths());
    statement.setEmiAmount(loan.getEmiAmount());
    statement.setTotalPayable(loan.getTotalPayable());
    statement.setAmountPaid(loan.getAmountPaid());
    statement.setRemainingBalance(loan.getRemainingBalance());
    statement.setLoanStatus(loan.getStatus());
    statement.setApplicationDate(loan.getApplicationDate());
    statement.setApprovalDate(loan.getApprovalDate());
    
    List<LoanStatementDTO.PaymentRecordDTO> paymentHistory = repayments.stream().map(repayment -> {
        LoanStatementDTO.PaymentRecordDTO record = new LoanStatementDTO.PaymentRecordDTO();
        record.setInstallmentNumber(repayment.getInstallmentNumber());
        record.setDueDate(repayment.getDueDate());
        record.setAmountDue(repayment.getAmountDue());
        record.setPaidDate(repayment.getPaidDate());
        record.setAmountPaid(repayment.getAmountPaid());
        record.setStatus(repayment.getStatus());
        record.setPaymentMethod(repayment.getPaymentMethod());
        record.setTransactionReference(repayment.getTransactionReference());
        return record;
    }).collect(Collectors.toList());
    
    statement.setPaymentHistory(paymentHistory);
    return statement;
}
    
    // Generate payment schedule for approved loan
    public List<Repayment> generatePaymentSchedule(Loan loan) {
        List<Repayment> schedule = new ArrayList<>();
        BigDecimal emiAmount = loan.getEmiAmount();
        
        for (int i = 1; i <= loan.getTenureMonths(); i++) {
            Repayment repayment = new Repayment();
            repayment.setLoan(loan);
            repayment.setInstallmentNumber(i);
            repayment.setDueDate(LocalDateTime.now().plusMonths(i));
            repayment.setAmountDue(emiAmount);
            repayment.setStatus("PENDING");
            schedule.add(repayment);
        }
        
        return repaymentRepository.saveAll(schedule);
    }
    
    // Record a payment
    public Repayment recordPayment(Long loanId, Integer installmentNumber, BigDecimal amountPaid, 
                                   String paymentMethod, String transactionReference) {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) return null;
        
        Repayment repayment = repaymentRepository.findByLoanAndInstallmentNumber(loan, installmentNumber).orElse(null);
        if (repayment == null) return null;
        
        repayment.setAmountPaid(amountPaid);
        repayment.setPaidDate(LocalDateTime.now());
        repayment.setStatus("PAID");
        repayment.setPaymentMethod(paymentMethod);
        repayment.setTransactionReference(transactionReference);
        
        // Update loan totals
        BigDecimal newAmountPaid = loan.getAmountPaid().add(amountPaid);
        loan.setAmountPaid(newAmountPaid);
        
        int newInstallmentsPaid = loan.getInstallmentsPaid() + 1;
        loan.setInstallmentsPaid(newInstallmentsPaid);
        
        BigDecimal remaining = loan.getTotalPayable().subtract(newAmountPaid);
        loan.setRemainingBalance(remaining);
        
        // Check if loan is fully paid
        if (newInstallmentsPaid >= loan.getTenureMonths()) {
            loan.setStatus("CLOSED");
        }
        
        loanRepository.save(loan);
        return repaymentRepository.save(repayment);
    }
    
    // Get payment history for a loan
    public List<Repayment> getPaymentHistory(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) return new ArrayList<>();
        return repaymentRepository.findByLoan(loan);
    }
    
    // Get loan summary using Map (simpler, no errors)
    public Map<String, Object> getLoanSummary(Long loanId) {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) return null;
        
        long paidCount = repaymentRepository.countByLoanAndStatus(loan, "PAID");
        long pendingCount = loan.getTenureMonths() - paidCount;
        double progressPercent = (paidCount * 100.0) / loan.getTenureMonths();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("loanId", loan.getId());
        summary.put("customerName", loan.getCustomer().getFullName());
        summary.put("totalLoan", loan.getLoanAmount());
        summary.put("totalPayable", loan.getTotalPayable());
        summary.put("amountPaid", loan.getAmountPaid());
        summary.put("remainingBalance", loan.getRemainingBalance());
        summary.put("emiAmount", loan.getEmiAmount());
        summary.put("totalInstallments", loan.getTenureMonths());
        summary.put("paidInstallments", paidCount);
        summary.put("pendingInstallments", pendingCount);
        summary.put("status", loan.getStatus());
        summary.put("progressPercent", progressPercent);
        
        return summary;
    }
}