package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import com.lms.loanmanagementsystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private EmailService emailService;
    
    // Create customer with account (username + password + bank details)
    @PostMapping("/create-with-account")
    public ResponseEntity<?> createCustomerWithAccount(@RequestBody Map<String, String> request) {
        try {
            String fullName = request.get("fullName");
            String email = request.get("email");
            String phone = request.get("phone");
            String address = request.get("address");
            String username = request.get("username");
            String password = request.get("password");
            String creditScore = request.get("creditScore");
            String nationalId = request.get("nationalId");
            
            // Bank Account Fields
            String bankName = request.get("bankName");
            String bankAccountNumber = request.get("bankAccountNumber");
            String bankAccountName = request.get("bankAccountName");
            String mobileMoneyNumber = request.get("mobileMoneyNumber");
            
            // Validate required fields
            if (fullName == null || fullName.isEmpty() || 
                email == null || email.isEmpty() || 
                phone == null || phone.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body("All fields are required: fullName, email, phone, username, password");
            }
            
            // Check if username already exists
            if (customerRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body("Username '" + username + "' already exists");
            }
            
            // Check if email already exists
            if (customerRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("Email '" + email + "' already exists");
            }
            
            // Check if national ID already exists (if provided)
            if (nationalId != null && !nationalId.isEmpty()) {
                Optional<Customer> existingNationalId = customerRepository.findByNationalId(nationalId);
                if (existingNationalId.isPresent()) {
                    return ResponseEntity.badRequest().body("National ID '" + nationalId + "' already exists");
                }
            }
            
            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address != null ? address : "");
            customer.setUsername(username);
            customer.setPassword(password);
            customer.setCreditScore(creditScore != null && !creditScore.isEmpty() ? Integer.parseInt(creditScore) : 0);
            customer.setKycStatus("PENDING");
            customer.setNationalId(nationalId != null && !nationalId.isEmpty() ? nationalId : null);
            
            // Set bank account details
            customer.setBankName(bankName != null && !bankName.isEmpty() ? bankName : null);
            customer.setBankAccountNumber(bankAccountNumber != null && !bankAccountNumber.isEmpty() ? bankAccountNumber : null);
            customer.setBankAccountName(bankAccountName != null && !bankAccountName.isEmpty() ? bankAccountName : null);
            customer.setMobileMoneyNumber(mobileMoneyNumber != null && !mobileMoneyNumber.isEmpty() ? mobileMoneyNumber : null);
            
            Customer saved = customerRepository.save(customer);
            
            // Send welcome email
            try {
                emailService.sendWelcomeEmail(saved, password);
                System.out.println("✅ Welcome email sent to: " + saved.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Failed to send welcome email: " + e.getMessage());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer created successfully");
            response.put("customerId", saved.getId());
            response.put("username", saved.getUsername());
            response.put("fullName", saved.getFullName());
            response.put("email", saved.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }
    
    // Get all customers
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    // Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Update customer
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        return customerRepository.findById(id)
            .map(customer -> {
                customer.setFullName(customerDetails.getFullName());
                customer.setEmail(customerDetails.getEmail());
                customer.setPhone(customerDetails.getPhone());
                customer.setAddress(customerDetails.getAddress());
                customer.setPanNumber(customerDetails.getPanNumber());
                customer.setNationalId(customerDetails.getNationalId());
                customer.setCreditScore(customerDetails.getCreditScore());
                customer.setKycStatus(customerDetails.getKycStatus());
                customer.setUsername(customerDetails.getUsername());
                customer.setPassword(customerDetails.getPassword());
                
                // Bank Account Fields
                customer.setBankName(customerDetails.getBankName());
                customer.setBankAccountNumber(customerDetails.getBankAccountNumber());
                customer.setBankAccountName(customerDetails.getBankAccountName());
                customer.setMobileMoneyNumber(customerDetails.getMobileMoneyNumber());
                
                Customer updatedCustomer = customerRepository.save(customer);
                return ResponseEntity.ok(updatedCustomer);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Delete customer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}