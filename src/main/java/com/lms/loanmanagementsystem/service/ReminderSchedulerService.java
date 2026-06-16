package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
public class ReminderSchedulerService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendPaymentReminders() {
        System.out.println("Checking for payment reminders at 9:00 AM");
        
        List<Loan> activeLoans = loanRepository.findByStatus("APPROVED");
        LocalDateTime now = LocalDateTime.now();
        
        for (Loan loan : activeLoans) {
            List<Repayment> repayments = repaymentRepository.findByLoan(loan);
            
            for (Repayment repayment : repayments) {
                if (repayment.getStatus().equals("PENDING")) {
                    long daysUntilDue = ChronoUnit.DAYS.between(now, repayment.getDueDate());
                    long daysOverdue = ChronoUnit.DAYS.between(repayment.getDueDate(), now);
                    
                    if (daysUntilDue == 7) {
                        emailService.sendSevenDayReminder(loan, repayment);
                        System.out.println("Sent 7-day reminder for loan: " + loan.getId());
                    }
                    
                    if (daysUntilDue == 3) {
                        emailService.sendThreeDayReminder(loan, repayment);
                        System.out.println("Sent 3-day reminder for loan: " + loan.getId());
                    }
                    
                    if (daysOverdue > 0 && daysOverdue <= 30) {
                        emailService.sendOverdueReminder(loan, repayment, (int) daysOverdue);
                        System.out.println("Sent overdue notice for loan: " + loan.getId());
                    }
                }
            }
        }
    }
}