package com.lms.loanmanagementsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class LoanStatementDTO {
    
    private Long loanId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal totalPayable;
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;
    private String loanStatus;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private List<PaymentRecordDTO> paymentHistory;
    
    // Inner class for payment records
    public static class PaymentRecordDTO {
        private Integer installmentNumber;
        private LocalDateTime dueDate;
        private BigDecimal amountDue;
        private LocalDateTime paidDate;
        private BigDecimal amountPaid;
        private String status;
        private String paymentMethod;
        private String transactionReference;
        
        // Getters and Setters
        public Integer getInstallmentNumber() { return installmentNumber; }
        public void setInstallmentNumber(Integer installmentNumber) { this.installmentNumber = installmentNumber; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        
        public BigDecimal getAmountDue() { return amountDue; }
        public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }
        
        public LocalDateTime getPaidDate() { return paidDate; }
        public void setPaidDate(LocalDateTime paidDate) { this.paidDate = paidDate; }
        
        public BigDecimal getAmountPaid() { return amountPaid; }
        public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    }
    
    // Getters and Setters
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    
    public BigDecimal getTotalPayable() { return totalPayable; }
    public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }
    
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    
    public String getLoanStatus() { return loanStatus; }
    public void setLoanStatus(String loanStatus) { this.loanStatus = loanStatus; }
    
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }
    
    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
    
    public List<PaymentRecordDTO> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(List<PaymentRecordDTO> paymentHistory) { this.paymentHistory = paymentHistory; }
}