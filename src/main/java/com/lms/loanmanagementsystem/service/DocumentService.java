package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Document;
import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.repository.DocumentRepository;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private LoanRepository loanRepository;

    private static final Map<String, String> DOCUMENT_TYPES = new HashMap<>();
    static {
        DOCUMENT_TYPES.put("ID_PROOF", "ID/Passport");
        DOCUMENT_TYPES.put("INCOME_PROOF", "Income/Salary Slip");
        DOCUMENT_TYPES.put("BANK_STATEMENT", "Bank Statement");
        DOCUMENT_TYPES.put("ADDRESS_PROOF", "Address Proof");
        DOCUMENT_TYPES.put("AGREEMENT", "Loan Agreement");
    }

    // Add document record
    public Document addDocument(Long loanId, String documentType, String uploadedBy) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return null;
        }
        
        Loan loan = loanOpt.get();
        
        // Check if document already exists for this loan and type
        List<Document> existingDocs = documentRepository.findByLoan(loan);
        boolean exists = existingDocs.stream().anyMatch(d -> d.getDocumentType().equals(documentType));
        
        Document document = new Document();
        document.setLoan(loan);
        document.setDocumentName(DOCUMENT_TYPES.getOrDefault(documentType, documentType));
        document.setDocumentType(documentType);
        document.setFileName("document_" + System.currentTimeMillis() + ".pdf");
        document.setFilePath("/uploads/sample.pdf");
        document.setFileType("application/pdf");
        document.setFileSize(1024L);
        document.setStatus("PENDING");
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(LocalDateTime.now());
        
        return documentRepository.save(document);
    }

    // Get all documents for a loan
    public List<Document> getDocumentsByLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return documentRepository.findByLoan(loanOpt.get());
    }

    // Verify document
    public Document verifyDocument(Long documentId, String status, String rejectionReason) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return null;
        }
        
        Document document = docOpt.get();
        document.setStatus(status);
        if (rejectionReason != null && !rejectionReason.isEmpty()) {
            document.setRejectionReason(rejectionReason);
        }
        return documentRepository.save(document);
    }

    // Delete document
    public boolean deleteDocument(Long documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return false;
        }
        documentRepository.delete(docOpt.get());
        return true;
    }

    // Get document summary
    public Map<String, Object> getDocumentSummary(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            return null;
        }
        
        Loan loan = loanOpt.get();
        List<Document> documents = documentRepository.findByLoan(loan);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDocuments", documents.size());
        summary.put("pendingCount", documents.stream().filter(d -> "PENDING".equals(d.getStatus())).count());
        summary.put("verifiedCount", documents.stream().filter(d -> "VERIFIED".equals(d.getStatus())).count());
        summary.put("rejectedCount", documents.stream().filter(d -> "REJECTED".equals(d.getStatus())).count());
        
        // Required document types
        List<String> requiredTypes = Arrays.asList("ID_PROOF", "INCOME_PROOF", "ADDRESS_PROOF");
        List<String> uploadedTypes = documents.stream().map(Document::getDocumentType).toList();
        
        List<String> missingTypes = new ArrayList<>();
        for (String required : requiredTypes) {
            if (!uploadedTypes.contains(required)) {
                missingTypes.add(DOCUMENT_TYPES.get(required));
            }
        }
        summary.put("missingDocuments", missingTypes);
        summary.put("complete", missingTypes.isEmpty());
        
        return summary;
    }
}