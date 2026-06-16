package com.lms.loanmanagementsystem.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class EligibilityService {

    // Eligibility criteria
    private static final int MIN_CREDIT_SCORE = 600;
    private static final int GOOD_CREDIT_SCORE = 700;
    private static final int EXCELLENT_CREDIT_SCORE = 800;
    
    // Income multiples for different loan types
    private static final Map<String, Integer> INCOME_MULTIPLES = new HashMap<>();
    static {
        INCOME_MULTIPLES.put("PERSONAL", 4);
        INCOME_MULTIPLES.put("HOME", 6);
        INCOME_MULTIPLES.put("AUTO", 3);
        INCOME_MULTIPLES.put("EDUCATION", 5);
    }
    
    // Interest rates based on credit score
    private static final Map<String, BigDecimal> INTEREST_RATES = new HashMap<>();
    static {
        INTEREST_RATES.put("POOR", new BigDecimal("18.0"));
        INTEREST_RATES.put("FAIR", new BigDecimal("15.0"));
        INTEREST_RATES.put("GOOD", new BigDecimal("12.0"));
        INTEREST_RATES.put("EXCELLENT", new BigDecimal("9.0"));
    }

    // Calculate maximum loan amount based on monthly income
    public Map<String, Object> calculateMaxLoanAmount(BigDecimal monthlyIncome, String loanType) {
        int multiplier = INCOME_MULTIPLES.getOrDefault(loanType, 4);
        BigDecimal maxLoanAmount = monthlyIncome.multiply(BigDecimal.valueOf(multiplier * 12));
        
        Map<String, Object> result = new HashMap<>();
        result.put("monthlyIncome", monthlyIncome);
        result.put("multiplier", multiplier);
        result.put("maxLoanAmount", maxLoanAmount);
        result.put("loanType", loanType);
        
        return result;
    }

    // Check eligibility based on credit score
    public Map<String, Object> checkEligibility(Integer creditScore, BigDecimal monthlyIncome, 
                                                  BigDecimal requestedAmount, String loanType) {
        Map<String, Object> result = new HashMap<>();
        List<String> messages = new ArrayList<>();
        boolean eligible = true;
        
        // Check credit score
        String creditRating;
        if (creditScore == null) {
            creditRating = "UNKNOWN";
            eligible = false;
            messages.add("Credit score not available. Please update your credit score.");
        } else if (creditScore >= EXCELLENT_CREDIT_SCORE) {
            creditRating = "EXCELLENT";
            messages.add("Excellent credit score! You qualify for best interest rates.");
        } else if (creditScore >= GOOD_CREDIT_SCORE) {
            creditRating = "GOOD";
            messages.add("Good credit score. You qualify for competitive rates.");
        } else if (creditScore >= MIN_CREDIT_SCORE) {
            creditRating = "FAIR";
            messages.add("Fair credit score. You may get standard interest rates.");
        } else {
            creditRating = "POOR";
            eligible = false;
            messages.add("Credit score too low. Minimum required: " + MIN_CREDIT_SCORE);
        }
        
        // Check requested amount vs maximum
        Map<String, Object> maxCalc = calculateMaxLoanAmount(monthlyIncome, loanType);
        BigDecimal maxAmount = (BigDecimal) maxCalc.get("maxLoanAmount");
        
        if (requestedAmount.compareTo(maxAmount) > 0) {
            eligible = false;
            messages.add("Requested amount exceeds maximum eligible amount of MWK " + maxAmount.toPlainString());
        }
        
        // Get interest rate based on credit rating
        BigDecimal interestRate;
        if (creditRating.equals("EXCELLENT")) {
            interestRate = INTEREST_RATES.get("EXCELLENT");
        } else if (creditRating.equals("GOOD")) {
            interestRate = INTEREST_RATES.get("GOOD");
        } else if (creditRating.equals("FAIR")) {
            interestRate = INTEREST_RATES.get("FAIR");
        } else {
            interestRate = INTEREST_RATES.get("POOR");
        }
        
        result.put("eligible", eligible);
        result.put("creditRating", creditRating);
        result.put("interestRate", interestRate);
        result.put("maxEligibleAmount", maxAmount);
        result.put("requestedAmount", requestedAmount);
        result.put("messages", messages);
        
        return result;
    }

    // Get loan offers with different tenures
    public List<Map<String, Object>> getLoanOffers(BigDecimal loanAmount, Integer creditScore) {
        List<Map<String, Object>> offers = new ArrayList<>();
        
        // Determine interest rate based on credit score
        BigDecimal interestRate;
        if (creditScore >= EXCELLENT_CREDIT_SCORE) {
            interestRate = INTEREST_RATES.get("EXCELLENT");
        } else if (creditScore >= GOOD_CREDIT_SCORE) {
            interestRate = INTEREST_RATES.get("GOOD");
        } else {
            interestRate = INTEREST_RATES.get("FAIR");
        }
        
        // Tenure options: 12, 24, 36, 48, 60 months
        int[] tenures = {12, 24, 36, 48, 60};
        
        for (int tenure : tenures) {
            BigDecimal monthlyEMI = calculateEMI(loanAmount, interestRate, tenure);
            BigDecimal totalPayable = monthlyEMI.multiply(BigDecimal.valueOf(tenure));
            BigDecimal totalInterest = totalPayable.subtract(loanAmount);
            
            Map<String, Object> offer = new HashMap<>();
            offer.put("tenureMonths", tenure);
            offer.put("interestRate", interestRate);
            offer.put("monthlyEMI", monthlyEMI.setScale(2, RoundingMode.HALF_UP));
            offer.put("totalPayable", totalPayable.setScale(2, RoundingMode.HALF_UP));
            offer.put("totalInterest", totalInterest.setScale(2, RoundingMode.HALF_UP));
            offers.add(offer);
        }
        
        return offers;
    }

    // Calculate EMI
    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        BigDecimal monthlyRate = annualRate
            .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(tenureMonths);
        
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    // Get credit score rating description
    public Map<String, Object> getCreditScoreInfo(Integer creditScore) {
        Map<String, Object> info = new HashMap<>();
        
        if (creditScore == null) {
            info.put("rating", "Unknown");
            info.put("color", "secondary");
            info.put("description", "Update your credit score to see eligibility");
            info.put("minRequired", MIN_CREDIT_SCORE);
        } else if (creditScore >= EXCELLENT_CREDIT_SCORE) {
            info.put("rating", "Excellent");
            info.put("color", "success");
            info.put("description", "Best interest rates available");
            info.put("interestRate", INTEREST_RATES.get("EXCELLENT"));
        } else if (creditScore >= GOOD_CREDIT_SCORE) {
            info.put("rating", "Good");
            info.put("color", "info");
            info.put("description", "Competitive interest rates");
            info.put("interestRate", INTEREST_RATES.get("GOOD"));
        } else if (creditScore >= MIN_CREDIT_SCORE) {
            info.put("rating", "Fair");
            info.put("color", "warning");
            info.put("description", "Standard interest rates apply");
            info.put("interestRate", INTEREST_RATES.get("FAIR"));
        } else {
            info.put("rating", "Poor");
            info.put("color", "danger");
            info.put("description", "Does not meet minimum requirements");
            info.put("interestRate", INTEREST_RATES.get("POOR"));
            info.put("minRequired", MIN_CREDIT_SCORE);
        }
        
        return info;
    }
}