package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CustomerRepository customerRepository;

    // Customer Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String password = request.get("password");
        
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        
        if (customerOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        Customer customer = customerOpt.get();
        
        // In production, use password encoder!
        if (!customer.getPassword().equals(password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid password"));
        }
        
        // Store customer in session
        session.setAttribute("customerId", customer.getId());
        session.setAttribute("customerName", customer.getFullName());
        session.setAttribute("role", "CUSTOMER");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("customerId", customer.getId());
        response.put("customerName", customer.getFullName());
        
        return ResponseEntity.ok(response);
    }
    
    // Customer Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String fullName = request.get("fullName");
        String email = request.get("email");
        String phone = request.get("phone");
        
        // Check if username exists
        if (customerRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(password); // In production, encode password!
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setKycStatus("PENDING");
        
        Customer saved = customerRepository.save(customer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("customerId", saved.getId());
        
        return ResponseEntity.ok(response);
    }
    
    // Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
    
    // Check if logged in
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Long customerId = (Long) session.getAttribute("customerId");
        if (customerId == null) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isEmpty()) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("loggedIn", true);
        response.put("customerId", customer.get().getId());
        response.put("customerName", customer.get().getFullName());
        response.put("email", customer.get().getEmail());
        
        return ResponseEntity.ok(response);
    }
}