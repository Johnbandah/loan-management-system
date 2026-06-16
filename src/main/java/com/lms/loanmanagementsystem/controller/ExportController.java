package com.lms.loanmanagementsystem.controller;

import com.lms.loanmanagementsystem.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    // Export all loans to Excel
    @GetMapping("/loans")
    public ResponseEntity<byte[]> exportLoans() {
        try {
            byte[] excelData = exportService.exportLoansToExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "loans_report.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Export payment history for a specific loan
    @GetMapping("/payments/{loanId}")
    public ResponseEntity<byte[]> exportPaymentHistory(@PathVariable Long loanId) {
        try {
            byte[] excelData = exportService.exportPaymentHistoryToExcel(loanId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "payment_history_loan_" + loanId + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Export all customers to Excel
    @GetMapping("/customers")
    public ResponseEntity<byte[]> exportCustomers() {
        try {
            byte[] excelData = exportService.exportCustomersToExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "customers_report.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}