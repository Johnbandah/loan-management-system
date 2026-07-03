package com.lms.loanmanagementsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "loans")
public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false)
    private BigDecimal loanAmount;
    
    @Column(nullable = false)
    private Integer tenureMonths;
    
    @Column(nullable = false)
    private BigDecimal interestRate;
    
    private BigDecimal emiAmount;
    
    private BigDecimal totalPayable;
    
    @Column(length = 50)
    private String loanType;
    
    @Column(length = 20)
    private String status = "PENDING";
    
    private String purpose;
    
    private LocalDateTime applicationDate = LocalDateTime.now();
    
    private LocalDateTime approvalDate;
    
    private String rejectionReason;
    
    // Repayment tracking fields
    private BigDecimal amountPaid = BigDecimal.ZERO;
    private BigDecimal remainingBalance;
    private Integer installmentsPaid = 0;
    
    // Disbursement tracking fields (NEW)
    @Column(nullable = false)
    private BigDecimal amountDisbursed = BigDecimal.ZERO;
    
    private LocalDateTime disbursementDate;
    
    private String disbursementMethod; // BANK_TRANSFER, MOBILE_MONEY, CASH, CHEQUE
    
    private String disbursementReference;
    
    private String disbursedBy;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    
    public BigDecimal getTotalPayable() { return totalPayable; }
    public void setTotalPayable(BigDecimal totalPayable) { this.totalPayable = totalPayable; }
    
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDateTime applicationDate) { this.applicationDate = applicationDate; }
    
    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    
    public BigDecimal getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    
    public Integer getInstallmentsPaid() { return installmentsPaid; }
    public void setInstallmentsPaid(Integer installmentsPaid) { this.installmentsPaid = installmentsPaid; }
    
    // NEW GETTERS AND SETTERS FOR DISBURSEMENT
    public BigDecimal getAmountDisbursed() { return amountDisbursed; }
    public void setAmountDisbursed(BigDecimal amountDisbursed) { this.amountDisbursed = amountDisbursed; }
    
    public LocalDateTime getDisbursementDate() { return disbursementDate; }
    public void setDisbursementDate(LocalDateTime disbursementDate) { this.disbursementDate = disbursementDate; }
    
    public String getDisbursementMethod() { return disbursementMethod; }
    public void setDisbursementMethod(String disbursementMethod) { this.disbursementMethod = disbursementMethod; }
    
    public String getDisbursementReference() { return disbursementReference; }
    public void setDisbursementReference(String disbursementReference) { this.disbursementReference = disbursementReference; }
    
    public String getDisbursedBy() { return disbursedBy; }
    public void setDisbursedBy(String disbursedBy) { this.disbursedBy = disbursedBy; }
}