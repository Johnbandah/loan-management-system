package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReminderService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;

    // Get upcoming payments (next 30 days)
    public List<Map<String, Object>> getUpcomingPayments() {
        List<Map<String, Object>> upcomingPayments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);
        
        List<Loan> activeLoans = loanRepository.findByStatus("APPROVED");
        
        for (Loan loan : activeLoans) {
            List<Repayment> repayments = repaymentRepository.findByLoan(loan);
            
            for (Repayment repayment : repayments) {
                if (repayment.getStatus().equals("PENDING") && 
                    repayment.getDueDate().isAfter(now) && 
                    repayment.getDueDate().isBefore(thirtyDaysLater)) {
                    
                    Map<String, Object> reminder = new HashMap<>();
                    reminder.put("loanId", loan.getId());
                    reminder.put("customerName", loan.getCustomer().getFullName());
                    reminder.put("installmentNumber", repayment.getInstallmentNumber());
                    reminder.put("dueDate", repayment.getDueDate());
                    reminder.put("amountDue", repayment.getAmountDue());
                    reminder.put("daysUntilDue", ChronoUnit.DAYS.between(now, repayment.getDueDate()));
                    
                    // Determine priority
                    int daysUntil = (int) ChronoUnit.DAYS.between(now, repayment.getDueDate());
                    if (daysUntil <= 3) {
                        reminder.put("priority", "URGENT");
                        reminder.put("color", "#e74c3c");
                    } else if (daysUntil <= 7) {
                        reminder.put("priority", "HIGH");
                        reminder.put("color", "#f39c12");
                    } else {
                        reminder.put("priority", "NORMAL");
                        reminder.put("color", "#3498db");
                    }
                    
                    upcomingPayments.add(reminder);
                }
            }
        }
        
        // Sort by due date
        upcomingPayments.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("dueDate");
            LocalDateTime dateB = (LocalDateTime) b.get("dueDate");
            return dateA.compareTo(dateB);
        });
        
        return upcomingPayments;
    }

    // Get overdue payments
    public List<Map<String, Object>> getOverduePayments() {
        List<Map<String, Object>> overduePayments = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        List<Loan> activeLoans = loanRepository.findByStatus("APPROVED");
        
        for (Loan loan : activeLoans) {
            List<Repayment> repayments = repaymentRepository.findByLoan(loan);
            
            for (Repayment repayment : repayments) {
                if (repayment.getStatus().equals("PENDING") && 
                    repayment.getDueDate().isBefore(now)) {
                    
                    Map<String, Object> reminder = new HashMap<>();
                    reminder.put("loanId", loan.getId());
                    reminder.put("customerName", loan.getCustomer().getFullName());
                    reminder.put("installmentNumber", repayment.getInstallmentNumber());
                    reminder.put("dueDate", repayment.getDueDate());
                    reminder.put("amountDue", repayment.getAmountDue());
                    reminder.put("daysOverdue", ChronoUnit.DAYS.between(repayment.getDueDate(), now));
                    reminder.put("priority", "OVERDUE");
                    reminder.put("color", "#e74c3c");
                    
                    overduePayments.add(reminder);
                }
            }
        }
        
        // Sort by days overdue (highest first)
        overduePayments.sort((a, b) -> {
            int daysA = (int) a.get("daysOverdue");
            int daysB = (int) b.get("daysOverdue");
            return Integer.compare(daysB, daysA);
        });
        
        return overduePayments;
    }

    // Get payment summary for dashboard
    public Map<String, Object> getPaymentSummary() {
        List<Map<String, Object>> upcoming = getUpcomingPayments();
        List<Map<String, Object>> overdue = getOverduePayments();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("upcomingCount", upcoming.size());
        summary.put("overdueCount", overdue.size());
        summary.put("upcomingAmount", upcoming.stream()
            .mapToDouble(r -> ((Number) r.get("amountDue")).doubleValue())
            .sum());
        summary.put("overdueAmount", overdue.stream()
            .mapToDouble(r -> ((Number) r.get("amountDue")).doubleValue())
            .sum());
        summary.put("urgentCount", (int) upcoming.stream()
            .filter(r -> r.get("priority").equals("URGENT"))
            .count());
        
        return summary;
    }
}