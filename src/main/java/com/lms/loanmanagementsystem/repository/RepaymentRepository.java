package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoan(Loan loan);
    List<Repayment> findByLoanAndStatus(Loan loan, String status);
    Optional<Repayment> findByLoanAndInstallmentNumber(Loan loan, Integer installmentNumber);
    long countByLoanAndStatus(Loan loan, String status);
}