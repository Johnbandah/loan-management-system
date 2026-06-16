package com.lms.loanmanagementsystem.service;

import com.lms.loanmanagementsystem.entity.Loan;
import com.lms.loanmanagementsystem.entity.Repayment;
import com.lms.loanmanagementsystem.repository.LoanRepository;
import com.lms.loanmanagementsystem.repository.RepaymentRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ReceiptService {

    @Autowired
    private LoanRepository loanRepository;
    
    @Autowired
    private RepaymentRepository repaymentRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public byte[] generatePaymentReceipt(Long paymentId) throws Exception {
        Optional<Repayment> repaymentOpt = repaymentRepository.findById(paymentId);
        if (repaymentOpt.isEmpty()) {
            throw new Exception("Payment not found with ID: " + paymentId);
        }
        
        Repayment repayment = repaymentOpt.get();
        Loan loan = repayment.getLoan();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();
        
        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        
        // Header
        Paragraph title = new Paragraph("LOAN MANAGEMENT SYSTEM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        Paragraph subtitle = new Paragraph("Official Payment Receipt", headerFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Receipt Info Table
        PdfPTable receiptTable = new PdfPTable(2);
        receiptTable.setWidthPercentage(100);
        receiptTable.setSpacingBefore(10);
        receiptTable.setSpacingAfter(10);
        
        addCell(receiptTable, "Receipt Number:", boldFont);
        addCell(receiptTable, "RCP-" + System.currentTimeMillis(), normalFont);
        
        addCell(receiptTable, "Receipt Date:", boldFont);
        addCell(receiptTable, LocalDateTime.now().format(DATE_FORMATTER), normalFont);
        
        addCell(receiptTable, "Payment ID:", boldFont);
        addCell(receiptTable, String.valueOf(repayment.getId()), normalFont);
        
        document.add(receiptTable);
        document.add(new Paragraph(" "));
        
        // Customer Information
        Paragraph customerTitle = new Paragraph("CUSTOMER INFORMATION", headerFont);
        customerTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(customerTitle);
        document.add(new Paragraph(" "));
        
        PdfPTable customerTable = new PdfPTable(2);
        customerTable.setWidthPercentage(100);
        
        addCell(customerTable, "Customer Name:", boldFont);
        addCell(customerTable, loan.getCustomer().getFullName(), normalFont);
        
        addCell(customerTable, "Customer Email:", boldFont);
        addCell(customerTable, loan.getCustomer().getEmail(), normalFont);
        
        addCell(customerTable, "Customer Phone:", boldFont);
        addCell(customerTable, loan.getCustomer().getPhone(), normalFont);
        
        addCell(customerTable, "Customer Address:", boldFont);
        addCell(customerTable, loan.getCustomer().getAddress() != null ? loan.getCustomer().getAddress() : "N/A", normalFont);
        
        document.add(customerTable);
        document.add(new Paragraph(" "));
        
        // Loan Information
        Paragraph loanTitle = new Paragraph("LOAN INFORMATION", headerFont);
        loanTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(loanTitle);
        document.add(new Paragraph(" "));
        
        PdfPTable loanTable = new PdfPTable(2);
        loanTable.setWidthPercentage(100);
        
        addCell(loanTable, "Loan ID:", boldFont);
        addCell(loanTable, String.valueOf(loan.getId()), normalFont);
        
        addCell(loanTable, "Loan Amount:", boldFont);
        addCell(loanTable, String.format("MWK %, .2f", loan.getLoanAmount()), normalFont);
        
        addCell(loanTable, "Interest Rate:", boldFont);
        addCell(loanTable, loan.getInterestRate() + "%", normalFont);
        
        addCell(loanTable, "Tenure:", boldFont);
        addCell(loanTable, loan.getTenureMonths() + " months", normalFont);
        
        document.add(loanTable);
        document.add(new Paragraph(" "));
        
        // Payment Details
        Paragraph paymentTitle = new Paragraph("PAYMENT DETAILS", headerFont);
        paymentTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(paymentTitle);
        document.add(new Paragraph(" "));
        
        PdfPTable paymentTable = new PdfPTable(2);
        paymentTable.setWidthPercentage(100);
        
        addCell(paymentTable, "Installment Number:", boldFont);
        addCell(paymentTable, String.valueOf(repayment.getInstallmentNumber()), normalFont);
        
        addCell(paymentTable, "Due Date:", boldFont);
        addCell(paymentTable, repayment.getDueDate().format(DATE_ONLY_FORMATTER), normalFont);
        
        addCell(paymentTable, "Amount Due:", boldFont);
        addCell(paymentTable, String.format("MWK %, .2f", repayment.getAmountDue()), normalFont);
        
        addCell(paymentTable, "Amount Paid:", boldFont);
        addCell(paymentTable, String.format("MWK %, .2f", repayment.getAmountPaid()), normalFont);
        
        addCell(paymentTable, "Payment Date:", boldFont);
        addCell(paymentTable, repayment.getPaidDate().format(DATE_FORMATTER), normalFont);
        
        addCell(paymentTable, "Payment Method:", boldFont);
        addCell(paymentTable, repayment.getPaymentMethod(), normalFont);
        
        addCell(paymentTable, "Transaction Reference:", boldFont);
        addCell(paymentTable, repayment.getTransactionReference(), normalFont);
        
        addCell(paymentTable, "Payment Status:", boldFont);
        addCell(paymentTable, "COMPLETED", normalFont);
        
        document.add(paymentTable);
        document.add(new Paragraph(" "));
        
        // Loan Summary
        Paragraph summaryTitle = new Paragraph("LOAN SUMMARY", headerFont);
        summaryTitle.setAlignment(Element.ALIGN_LEFT);
        document.add(summaryTitle);
        document.add(new Paragraph(" "));
        
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{40f, 30f, 30f});
        
        addCell(summaryTable, "Description", boldFont);
        addCell(summaryTable, "Amount (MWK)", boldFont);
        addCell(summaryTable, "Status", boldFont);
        
        addCell(summaryTable, "Total Loan Amount", normalFont);
        addCell(summaryTable, String.format("MWK %, .2f", loan.getTotalPayable()), normalFont);
        addCell(summaryTable, "PENDING", normalFont);
        
        addCell(summaryTable, "Amount Paid to Date", normalFont);
        addCell(summaryTable, String.format("MWK %, .2f", loan.getAmountPaid()), normalFont);
        addCell(summaryTable, "PAID", normalFont);
        
        BigDecimal remaining = loan.getTotalPayable().subtract(loan.getAmountPaid());
        addCell(summaryTable, "Remaining Balance", normalFont);
        addCell(summaryTable, String.format("MWK %, .2f", remaining), normalFont);
        addCell(summaryTable, "PENDING", normalFont);
        
        document.add(summaryTable);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        
        // Footer
        Paragraph footer = new Paragraph("This is a computer-generated receipt. No signature is required.", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        Paragraph thankYou = new Paragraph("Thank you for your payment!", boldFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        document.add(thankYou);
        
        document.close();
        
        return out.toByteArray();
    }
    
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        table.addCell(cell);
    }
}