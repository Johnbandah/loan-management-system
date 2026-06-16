package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    // Create a new customer
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
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
    
    // Update customer (full update)
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails) {
        return customerRepository.findById(id)
            .map(customer -> {
                customer.setFullName(customerDetails.getFullName());
                customer.setEmail(customerDetails.getEmail());
                customer.setPhone(customerDetails.getPhone());
                customer.setAddress(customerDetails.getAddress());
                customer.setPanNumber(customerDetails.getPanNumber());
                customer.setCreditScore(customerDetails.getCreditScore());
                customer.setKycStatus(customerDetails.getKycStatus());
                customer.setUsername(customerDetails.getUsername());
                customer.setPassword(customerDetails.getPassword());
                
                Customer updatedCustomer = customerRepository.save(customer);
                return ResponseEntity.ok(updatedCustomer);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Partial update for username/password only
    @PatchMapping("/{id}/credentials")
    public ResponseEntity<?> updateCredentials(@PathVariable Long id, @RequestBody Map<String, String> credentials) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Customer customer = customerOpt.get();
        
        // Update username if provided
        if (credentials.containsKey("username")) {
            customer.setUsername(credentials.get("username"));
        }
        
        // Update password if provided
        if (credentials.containsKey("password")) {
            customer.setPassword(credentials.get("password"));
        }
        
        Customer updated = customerRepository.save(customer);
        
        return ResponseEntity.ok(Map.of(
            "message", "Credentials updated successfully",
            "id", updated.getId(),
            "username", updated.getUsername()
        ));
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