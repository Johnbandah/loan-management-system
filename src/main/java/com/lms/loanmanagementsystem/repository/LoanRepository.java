package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByCustomer(Customer customer);
    List<Loan> findByStatus(String status);
    List<Loan> findByCustomerId(Long customerId);
        List<Loan> findByApplicationDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);
}