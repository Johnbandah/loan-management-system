package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Customer;
import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.CustomerRepository;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Export all loans to Excel
    public byte[] exportLoansToExcel() throws IOException {
        List<Loan> loans = loanRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Loans Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row header = sheet.createRow(0);
            String[] columns = {"Loan ID", "Customer Name", "Loan Amount (MWK)", "Tenure (Months)", 
                               "EMI (MWK)", "Total Payable (MWK)", "Amount Paid (MWK)", 
                               "Remaining (MWK)", "Status", "Application Date", "Approval Date"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data rows
            int rowNum = 1;
            for (Loan loan : loans) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(loan.getId());
                row.createCell(1).setCellValue(loan.getCustomer().getFullName());
                row.createCell(2).setCellValue(loan.getLoanAmount().doubleValue());
                row.createCell(3).setCellValue(loan.getTenureMonths());
                row.createCell(4).setCellValue(loan.getEmiAmount() != null ? loan.getEmiAmount().doubleValue() : 0);
                row.createCell(5).setCellValue(loan.getTotalPayable() != null ? loan.getTotalPayable().doubleValue() : 0);
                row.createCell(6).setCellValue(loan.getAmountPaid() != null ? loan.getAmountPaid().doubleValue() : 0);
                row.createCell(7).setCellValue(loan.getRemainingBalance() != null ? loan.getRemainingBalance().doubleValue() : 0);
                row.createCell(8).setCellValue(loan.getStatus());
                row.createCell(9).setCellValue(loan.getApplicationDate().format(DATE_FORMATTER));
                row.createCell(10).setCellValue(loan.getApprovalDate() != null ? loan.getApprovalDate().format(DATE_FORMATTER) : "-");
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Export payment history for a specific loan to Excel
    public byte[] exportPaymentHistoryToExcel(Long loanId) throws IOException {
        Loan loan = loanRepository.findById(loanId).orElse(null);
        if (loan == null) return new byte[0];
        
        List<Repayment> repayments = repaymentRepository.findByLoan(loan);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payment History - Loan " + loanId);
            
            // Add loan summary at top
            Row summaryRow = sheet.createRow(0);
            summaryRow.createCell(0).setCellValue("LOAN SUMMARY");
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle boldStyle = workbook.createCellStyle();
            boldStyle.setFont(boldFont);
            summaryRow.getCell(0).setCellStyle(boldStyle);
            
            Row customerRow = sheet.createRow(1);
            customerRow.createCell(0).setCellValue("Customer:");
            customerRow.createCell(1).setCellValue(loan.getCustomer().getFullName());
            
            Row amountRow = sheet.createRow(2);
            amountRow.createCell(0).setCellValue("Loan Amount:");
            amountRow.createCell(1).setCellValue(loan.getLoanAmount().doubleValue());
            
            Row emiRow = sheet.createRow(3);
            emiRow.createCell(0).setCellValue("Monthly EMI:");
            emiRow.createCell(1).setCellValue(loan.getEmiAmount() != null ? loan.getEmiAmount().doubleValue() : 0);
            
            Row statusRow = sheet.createRow(4);
            statusRow.createCell(0).setCellValue("Status:");
            statusRow.createCell(1).setCellValue(loan.getStatus());
            
            // Skip a row
            sheet.createRow(5);
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row for payment history
            Row header = sheet.createRow(6);
            String[] columns = {"Installment #", "Due Date", "Amount Due (MWK)", 
                               "Paid Date", "Amount Paid (MWK)", "Status", "Payment Method", "Reference"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add payment data rows
            int rowNum = 7;
            for (Repayment repayment : repayments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(repayment.getInstallmentNumber());
                row.createCell(1).setCellValue(repayment.getDueDate().format(DATE_FORMATTER));
                row.createCell(2).setCellValue(repayment.getAmountDue().doubleValue());
                row.createCell(3).setCellValue(repayment.getPaidDate() != null ? repayment.getPaidDate().format(DATE_FORMATTER) : "-");
                row.createCell(4).setCellValue(repayment.getAmountPaid() != null ? repayment.getAmountPaid().doubleValue() : 0);
                row.createCell(5).setCellValue(repayment.getStatus());
                row.createCell(6).setCellValue(repayment.getPaymentMethod() != null ? repayment.getPaymentMethod() : "-");
                row.createCell(7).setCellValue(repayment.getTransactionReference() != null ? repayment.getTransactionReference() : "-");
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Export all customers to Excel
    public byte[] exportCustomersToExcel() throws IOException {
        List<Customer> customers = customerRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers Report");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row header = sheet.createRow(0);
            String[] columns = {"Customer ID", "Full Name", "Email", "Phone", "Address", 
                               "PAN Number", "Credit Score", "KYC Status", "Created At"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getFullName());
                row.createCell(2).setCellValue(customer.getEmail());
                row.createCell(3).setCellValue(customer.getPhone());
                row.createCell(4).setCellValue(customer.getAddress() != null ? customer.getAddress() : "-");
                row.createCell(5).setCellValue(customer.getPanNumber() != null ? customer.getPanNumber() : "-");
                row.createCell(6).setCellValue(customer.getCreditScore() != null ? customer.getCreditScore() : 0);
                row.createCell(7).setCellValue(customer.getKycStatus());
                row.createCell(8).setCellValue(customer.getCreatedAt().format(DATE_FORMATTER));
            }
            
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}