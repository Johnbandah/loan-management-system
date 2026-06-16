package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingPayments() {
        return ResponseEntity.ok(reminderService.getUpcomingPayments());
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverduePayments() {
        return ResponseEntity.ok(reminderService.getOverduePayments());
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getPaymentSummary() {
        return ResponseEntity.ok(reminderService.getPaymentSummary());
    }
}