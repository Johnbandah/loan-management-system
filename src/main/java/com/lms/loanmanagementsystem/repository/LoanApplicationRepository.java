package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.LoanApplication;
import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByCustomer(Customer customer);
    List<LoanApplication> findByStatus(String status);
    List<LoanApplication> findByCustomerAndStatus(Customer customer, String status);
}