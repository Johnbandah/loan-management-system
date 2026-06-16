package com.lms.loanmanagementsystem.repository;

import com.lms.loanmanagementsystem.entity.Document;
import com.lms.loanmanagementsystem.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByLoan(Loan loan);
    List<Document> findByLoanAndStatus(Loan loan, String status);
    long countByLoanAndStatus(Loan loan, String status);
}