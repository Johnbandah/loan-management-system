package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.entity.Document;
import com.lms.loanmanagementsystem.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // Add document record
    @PostMapping("/add/{loanId}")
    public ResponseEntity<?> addDocument(
            @PathVariable Long loanId,
            @RequestParam String documentType,
            @RequestParam String uploadedBy) {
        try {
            Document document = documentService.addDocument(loanId, documentType, uploadedBy);
            if (document == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Loan not found with ID: " + loanId);
                return ResponseEntity.badRequest().body(error);
            }
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get all documents for a loan
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<Document>> getDocuments(@PathVariable Long loanId) {
        List<Document> documents = documentService.getDocumentsByLoan(loanId);
        return ResponseEntity.ok(documents);
    }

    // Verify document
    @PutMapping("/verify/{documentId}")
    public ResponseEntity<?> verifyDocument(
            @PathVariable Long documentId,
            @RequestParam String status,
            @RequestParam(required = false) String rejectionReason) {
        Document document = documentService.verifyDocument(documentId, status, rejectionReason);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }

    // Delete document
    @DeleteMapping("/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        boolean deleted = documentService.deleteDocument(documentId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Get document summary
    @GetMapping("/summary/{loanId}")
    public ResponseEntity<?> getDocumentSummary(@PathVariable Long loanId) {
        Map<String, Object> summary = documentService.getDocumentSummary(loanId);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }
}       