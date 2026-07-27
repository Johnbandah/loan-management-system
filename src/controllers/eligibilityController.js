const Customer = require('../models/Customer');
const Loan = require('../models/Loan');
const LoanApplication = require('../models/LoanApplication');

exports.checkEligibility = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const { requestedAmount, tenureMonths } = req.body;

    const customer = await Customer.findById(customerId);
    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }

    // Check existing loans
    const existingLoans = await Loan.find({ 
      customer: customerId,
      status: { $in: ['ACTIVE', 'APPROVED'] }
    });

    const totalOutstanding = existingLoans.reduce((sum, loan) => {
      return sum + (loan.remainingBalance || 0);
    }, 0);

    // Calculate max loan amount (simplified logic)
    const monthlyIncome = customer.creditScore > 700 ? 1000000 : 500000;
    const maxLoanAmount = Math.min(monthlyIncome * 10, 5000000);
    const maxTenure = 60;

    const eligible = requestedAmount <= maxLoanAmount && 
                     tenureMonths <= maxTenure &&
                     totalOutstanding < maxLoanAmount * 0.7;

    const reasons = [];
    if (requestedAmount > maxLoanAmount) {
      reasons.push(`Requested amount exceeds maximum allowed (MWK ${maxLoanAmount.toLocaleString()})`);
    }
    if (tenureMonths > maxTenure) {
      reasons.push(`Tenure exceeds maximum allowed (${maxTenure} months)`);
    }
    if (totalOutstanding >= maxLoanAmount * 0.7) {
      reasons.push(`Outstanding loans too high (MWK ${totalOutstanding.toLocaleString()})`);
    }

    res.json({
      success: true,
      eligible,
      reasons,
      maxLoanAmount,
      maxTenure,
      totalOutstanding,
      creditScore: customer.creditScore || 0
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getCreditScore = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const customer = await Customer.findById(customerId);
    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }

    // Calculate credit score (simplified)
    const loans = await Loan.find({ customer: customerId });
    const payments = await LoanApplication.find({ customer: customerId });
    
    let score = 600; // Base score
    if (customer.creditScore) {
      score = customer.creditScore;
    } else {
      // Add points for good behavior
      if (loans.length > 0) {
        const paidLoans = loans.filter(l => l.status === 'CLOSED');
        const onTimePayments = loans.filter(l => l.amountPaid >= l.loanAmount * 0.8);
        score += paidLoans.length * 10;
        score += onTimePayments.length * 5;
      }
      if (payments.length > 0) {
        const approved = payments.filter(p => p.status === 'APPROVED');
        score += approved.length * 5;
      }
      score = Math.min(Math.max(score, 300), 850);
      
      // Save calculated score
      customer.creditScore = score;
      await customer.save();
    }

    res.json({
      success: true,
      creditScore: score,
      rating: score >= 750 ? 'EXCELLENT' : 
              score >= 650 ? 'GOOD' : 
              score >= 550 ? 'FAIR' : 
              'POOR'
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};