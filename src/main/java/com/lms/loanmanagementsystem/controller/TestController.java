package com.lms.loanmanagementsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/hello")
    public Map<String, Object> sayHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Loan Management System is Running!");
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    @GetMapping("/test-email")
    public String testEmail(@RequestParam String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("johnbydon@gmail.com");
            message.setTo(to);
            message.setSubject("Test Email from Loan Management System");
            message.setText("Hello!\n\nThis is a test email from your Loan Management System.\n\nIf you received this, email is working correctly!\n\nBest regards,\nLMS Team");
            mailSender.send(message);
            return "✅ Email sent successfully to " + to;
        } catch (Exception e) {
            return "❌ Failed to send email: " + e.getMessage();
        }
    }
}