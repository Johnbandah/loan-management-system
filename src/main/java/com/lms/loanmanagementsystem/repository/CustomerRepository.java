package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // Find customer by email (unique)
    Optional<Customer> findByEmail(String email);
    
    // Find customer by phone number (unique)
    Optional<Customer> findByPhone(String phone);
    
    // Find customer by PAN number (unique)
    Optional<Customer> findByPanNumber(String panNumber);
    
    // Find customer by username (unique) - FOR LOGIN
    Optional<Customer> findByUsername(String username);
    
    // Check if email already exists
    boolean existsByEmail(String email);
    
    // Check if username already exists
    boolean existsByUsername(String username);
}