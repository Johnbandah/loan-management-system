package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.entity.LoanApplication;
import com.lms.loanmanagementsystem.entity.TopUpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    // ==================== LOAN APPROVAL EMAIL ====================
    public void sendLoanApprovalEmail(Loan loan) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "✅ Loan Approved - Loan Management System";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "Congratulations! Your loan application has been APPROVED.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "LOAN DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Loan Amount: MWK %.2f%n" +
                "Tenure: %d months%n" +
                "Interest Rate: %.2f%%%n" +
                "Monthly EMI: MWK %.2f%n" +
                "Total Payable: MWK %.2f%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Your first payment is due on %s.%n%n" +
                "Thank you for choosing our service!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                loan.getId(),
                loan.getLoanAmount(),
                loan.getTenureMonths(),
                loan.getInterestRate(),
                loan.getEmiAmount(),
                loan.getTotalPayable(),
                loan.getApprovalDate().plusMonths(1).format(DATE_FORMATTER)
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Approval email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send approval email: " + e.getMessage());
        }
    }

    // ==================== PAYMENT CONFIRMATION EMAIL ====================
    public void sendPaymentConfirmationEmail(Repayment repayment, Loan loan) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "💰 Payment Received - Loan Management System";
            
            BigDecimal remainingBalance = loan.getTotalPayable().subtract(loan.getAmountPaid());
            
            String body = String.format(
                "Dear %s,%n%n" +
                "We have received your payment successfully!%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "PAYMENT DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Installment #: %d%n" +
                "Amount Paid: MWK %.2f%n" +
                "Payment Method: %s%n" +
                "Transaction Reference: %s%n" +
                "Payment Date: %s%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "LOAN SUMMARY:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Total Paid: MWK %.2f%n" +
                "Remaining Balance: MWK %.2f%n" +
                "Installments Paid: %d of %d%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Thank you for your timely payment!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                loan.getId(),
                repayment.getInstallmentNumber(),
                repayment.getAmountPaid(),
                repayment.getPaymentMethod(),
                repayment.getTransactionReference(),
                repayment.getPaidDate().format(DATE_FORMATTER),
                loan.getAmountPaid(),
                remainingBalance,
                loan.getInstallmentsPaid(),
                loan.getTenureMonths()
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Payment confirmation email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send payment email: " + e.getMessage());
        }
    }

    // ==================== WELCOME EMAIL ====================
    public void sendWelcomeEmail(Customer customer, String password) {
        if (!emailEnabled) return;
        
        try {
            String subject = "Welcome to Loan Management System";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "Welcome to the Loan Management System!%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "YOUR LOGIN DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Username: %s%n" +
                "Password: %s%n" +
                "Login URL: http://localhost:8080/customer-login.html%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Please login and change your password for security purposes.%n%n" +
                "Thank you for choosing our services!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                customer.getUsername(),
                password
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Welcome email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email: " + e.getMessage());
        }
    }

    // ==================== DISBURSEMENT EMAIL ====================
    public void sendLoanDisbursementEmail(Loan loan) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "💰 Loan Disbursed - Loan Management System";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "Your loan of MWK %.2f has been DISBURSED to your account.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "DISBURSEMENT DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Amount Disbursed: MWK %.2f%n" +
                "Disbursement Method: %s%n" +
                "Reference: %s%n" +
                "Disbursement Date: %s%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Your first payment is due on %s.%n%n" +
                "Thank you for choosing our service!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                loan.getLoanAmount(),
                loan.getId(),
                loan.getAmountDisbursed(),
                loan.getDisbursementMethod(),
                loan.getDisbursementReference(),
                loan.getDisbursementDate().format(DATE_FORMATTER),
                loan.getDisbursementDate().plusMonths(1).format(DATE_FORMATTER)
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Disbursement email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send disbursement email: " + e.getMessage());
        }
    }

    // ==================== 7-DAY REMINDER ====================
    public void sendSevenDayReminder(Loan loan, Repayment repayment) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "🔔 Payment Reminder - 7 Days Left";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "This is a friendly reminder that your loan payment is due in 7 days.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "REMINDER DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Installment #: %d%n" +
                "Amount Due: MWK %.2f%n" +
                "Due Date: %s%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "You can make your payment through:%n" +
                "• Bank Transfer%n" +
                "• Mobile Money (Airtel Money / TNM Mpamba)%n" +
                "• Cash at our branch%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                loan.getId(),
                repayment.getInstallmentNumber(),
                repayment.getAmountDue(),
                repayment.getDueDate().format(DATE_FORMATTER)
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ 7-day reminder sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send 7-day reminder: " + e.getMessage());
        }
    }

    // ==================== 3-DAY URGENT REMINDER ====================
    public void sendThreeDayReminder(Loan loan, Repayment repayment) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "⚠️ URGENT: Payment Due in 3 Days!";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "⚠️ URGENT REMINDER: Your loan payment is due in 3 days!%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "PAYMENT DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Installment #: %d%n" +
                "Amount Due: MWK %.2f%n" +
                "Due Date: %s%n" +
                "Days Left: 3%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Please make your payment as soon as possible to avoid late fees.%n%n" +
                "Login to pay now: http://localhost:8080/customer-login.html%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                loan.getId(),
                repayment.getInstallmentNumber(),
                repayment.getAmountDue(),
                repayment.getDueDate().format(DATE_FORMATTER)
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ 3-day urgent reminder sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send urgent reminder: " + e.getMessage());
        }
    }

    // ==================== OVERDUE REMINDER ====================
    public void sendOverdueReminder(Loan loan, Repayment repayment, int daysOverdue) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = loan.getCustomer();
            String subject = "❗ PAYMENT OVERDUE - Action Required";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "❗ IMPORTANT: Your loan payment is now OVERDUE by %d days!%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "OVERDUE DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Loan ID: %d%n" +
                "Installment #: %d%n" +
                "Amount Due: MWK %.2f%n" +
                "Original Due Date: %s%n" +
                "Days Overdue: %d%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Please make your payment IMMEDIATELY to avoid:%n" +
                "• Late payment fees%n" +
                "• Negative impact on your credit score%n" +
                "• Account restriction%n%n" +
                "If you have already made the payment, please ignore this message.%n%n" +
                "Contact us immediately if you are facing difficulties.%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                daysOverdue,
                loan.getId(),
                repayment.getInstallmentNumber(),
                repayment.getAmountDue(),
                repayment.getDueDate().format(DATE_FORMATTER),
                daysOverdue
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Overdue reminder sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send overdue reminder: " + e.getMessage());
        }
    }

    // ==================== LOAN APPLICATION CONFIRMATION ====================
    public void sendLoanApplicationConfirmationEmail(LoanApplication application) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = application.getCustomer();
            String subject = "📋 Loan Application Received";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "We have received your loan application and it is currently under review.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "APPLICATION DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Application ID: %d%n" +
                "Requested Amount: MWK %.2f%n" +
                "Tenure: %d months%n" +
                "Loan Type: %s%n" +
                "Status: %s%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "You will receive an email once your application has been reviewed.%n%n" +
                "Thank you for choosing our services!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                application.getId(),
                application.getRequestedAmount(),
                application.getTenureMonths(),
                application.getLoanType(),
                application.getStatus()
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Application confirmation email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send confirmation email: " + e.getMessage());
        }
    }

    // ==================== LOAN APPLICATION STATUS UPDATE ====================
    public void sendLoanApplicationStatusEmail(LoanApplication application) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = application.getCustomer();
            String subject = "📋 Loan Application Status Update";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "Your loan application has been %s.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "APPLICATION DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Application ID: %d%n" +
                "Requested Amount: MWK %.2f%n" +
                "Status: %s%n" +
                "%s" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Thank you for choosing our services!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                application.getStatus().toLowerCase(),
                application.getId(),
                application.getRequestedAmount(),
                application.getStatus(),
                application.getRejectionReason() != null && !application.getRejectionReason().isEmpty() 
                    ? "Rejection Reason: " + application.getRejectionReason() + "%n" 
                    : ""
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Status update email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send status email: " + e.getMessage());
        }
    }
    

    // ==================== TOP-UP APPROVAL EMAIL ====================
    public void sendTopUpApprovalEmail(TopUpRequest request, Loan newLoan) {
        if (!emailEnabled) return;
        
        try {
            Customer customer = request.getCustomer();
            String subject = "✅ Top-up Approved - Additional Loan Disbursed";
            
            String body = String.format(
                "Dear %s,%n%n" +
                "Congratulations! Your top-up request has been APPROVED.%n%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "TOP-UP DETAILS:%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
                "Requested Amount: MWK %.2f%n" +
                "Approved Amount: MWK %.2f%n" +
                "New Loan ID: %d%n" +
                "Monthly EMI: MWK %.2f%n" +
                "Tenure: 12 months%n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n%n" +
                "Thank you for banking with us!%n%n" +
                "Best regards,%n" +
                "Loan Management System Team",
                customer.getFullName(),
                request.getRequestedAmount(),
                newLoan.getLoanAmount(),
                newLoan.getId(),
                newLoan.getEmiAmount()
            );
            
            sendEmail(customer.getEmail(), subject, body);
            System.out.println("✅ Top-up approval email sent to: " + customer.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Failed to send top-up email: " + e.getMessage());
        }
    }

    // ==================== GENERIC EMAIL SENDER ====================
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("📧 Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
            throw e;
        }
    }
}