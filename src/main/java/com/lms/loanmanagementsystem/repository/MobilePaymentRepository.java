package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.MobilePayment;
import com.lms.loanmanagementsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MobilePaymentRepository extends JpaRepository<MobilePayment, Long> {
    List<MobilePayment> findByCustomer(Customer customer);
    List<MobilePayment> findByCustomerAndStatus(Customer customer, String status);
}