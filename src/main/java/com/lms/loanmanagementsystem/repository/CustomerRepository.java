package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByPhone(String phone);
    
    Optional<Customer> findByPanNumber(String panNumber);
    
    Optional<Customer> findByUsername(String username);
    
    Optional<Customer> findByNationalId(String nationalId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}