package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.TopUpRequest;
import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TopUpRepository extends JpaRepository<TopUpRequest, Long> {
    List<TopUpRequest> findByCustomer(Customer customer);
    List<TopUpRequest> findByCustomerAndStatus(Customer customer, String status);
    List<TopUpRequest> findByStatus(String status);
}