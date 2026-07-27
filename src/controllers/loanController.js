const Loan = require('../models/Loan');
const LoanApplication = require('../models/LoanApplication');
const Notification = require('../models/Notification');
const Repayment = require('../models/Repayment');

// ============================================================
//  GET ALL LOANS
// ============================================================
exports.getAllLoans = async (req, res) => {
  try {
    const loans = await Loan.find()
      .populate('customer', 'fullName email phone')
      .populate('application', 'loanType purpose')
      .sort({ createdAt: -1 });
    res.json(loans);
  } catch (error) {
    console.error('Get all loans error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  GET LOANS BY CUSTOMER
// ============================================================
exports.getLoansByCustomer = async (req, res) => {
  try {
    const loans = await Loan.find({ customer: req.params.customerId })
      .populate('customer', 'fullName email phone')
      .sort({ createdAt: -1 });
    res.json(loans);
  } catch (error) {
    console.error('Get loans by customer error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  APPLY LOAN
// ============================================================
exports.applyLoan = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const { loanAmount, tenureMonths, interestRate, loanType, purpose } = req.body;

    // Calculate EMI
    const P = loanAmount;
    const r = interestRate / 100 / 12;
    const n = tenureMonths;
    let emiAmount;

    if (r === 0) {
      emiAmount = P / n;
    } else {
      emiAmount = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
    }

    const totalPayable = emiAmount * n;

    // Create loan application
    const application = await LoanApplication.create({
      customer: customerId,
      requestedAmount: loanAmount,
      tenureMonths,
      interestRate: interestRate || 12.5,
      loanType,
      purpose,
      status: 'PENDING'
    });

    // Create notification for admin
    await Notification.create({
      title: 'New Loan Application',
      message: `Customer applied for loan of MWK ${loanAmount.toLocaleString()}`,
      type: 'INFO',
      isAdmin: true
    });

    res.status(201).json({
      success: true,
      message: 'Loan application submitted successfully',
      applicationId: application._id,
      emiAmount: Math.round(emiAmount)
    });
  } catch (error) {
    console.error('Apply loan error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  APPROVE LOAN - FIXED
// ============================================================
exports.approveLoan = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    
    // Find the loan
    const loan = await Loan.findById(loanId).populate('customer');
    
    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }

    // Check if already approved
    if (loan.status === 'APPROVED') {
      return res.status(400).json({ success: false, message: 'Loan already approved' });
    }

    // Update status
    loan.status = 'APPROVED';
    await loan.save();

    // Generate repayment schedule
    await generateRepayments(loanId);

    // Notify customer
    await Notification.create({
      customer: loan.customer._id,
      title: 'Loan Approved',
      message: `Your loan of MWK ${loan.loanAmount.toLocaleString()} has been approved!`,
      type: 'SUCCESS'
    });

    res.json({ success: true, message: 'Loan approved successfully' });
  } catch (error) {
    console.error('Approve loan error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  DISBURSE LOAN
// ============================================================
exports.disburseLoan = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    const { method, reference, disbursedBy } = req.body;

    const loan = await Loan.findById(loanId).populate('customer');
    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }

    // Check if already disbursed
    if (loan.status === 'ACTIVE') {
      return res.status(400).json({ success: false, message: 'Loan already disbursed' });
    }

    loan.status = 'ACTIVE';
    loan.amountDisbursed = loan.loanAmount;
    loan.disbursementDate = new Date();
    loan.disbursementMethod = method;
    loan.disbursementReference = reference;
    await loan.save();

    // Notify customer
    await Notification.create({
      customer: loan.customer._id,
      title: 'Loan Disbursed',
      message: `Your loan of MWK ${loan.loanAmount.toLocaleString()} has been disbursed to your account.`,
      type: 'SUCCESS'
    });

    res.json({
      success: true,
      message: 'Loan disbursed successfully',
      loanAmount: loan.loanAmount
    });
  } catch (error) {
    console.error('Disburse loan error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  CALCULATE EMI
// ============================================================
exports.calculateEMI = async (req, res) => {
  try {
    const { amount, tenure, rate } = req.query;
    const P = parseFloat(amount);
    const n = parseInt(tenure);
    const r = parseFloat(rate) / 100 / 12;

    let monthlyEMI, totalPayable, totalInterest;

    if (r === 0) {
      monthlyEMI = P / n;
    } else {
      monthlyEMI = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
    }

    totalPayable = monthlyEMI * n;
    totalInterest = totalPayable - P;

    res.json({
      monthlyEMI: Math.round(monthlyEMI),
      totalPayable: Math.round(totalPayable),
      totalInterest: Math.round(totalInterest),
      principalAmount: P,
      tenureMonths: n
    });
  } catch (error) {
    console.error('Calculate EMI error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  GET LOAN BY ID - Add this method
// ============================================================
exports.getLoanById = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    const loan = await Loan.findById(loanId)
      .populate('customer', 'fullName email phone');
    
    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }
    
    res.json(loan);
  } catch (error) {
    console.error('Get loan by ID error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  GENERATE REPAYMENTS - Helper function
// ============================================================
async function generateRepayments(loanId) {
  try {
    const loan = await Loan.findById(loanId);
    if (!loan) {
      console.error('Loan not found for repayment generation:', loanId);
      return;
    }

    // Check if repayments already exist
    const existing = await Repayment.findOne({ loan: loanId });
    if (existing) {
      console.log('Repayments already exist for loan:', loanId);
      return;
    }

    const emiAmount = loan.emiAmount;
    const startDate = new Date();
    const repayments = [];

    for (let i = 1; i <= loan.tenureMonths; i++) {
      const dueDate = new Date(startDate);
      dueDate.setMonth(dueDate.getMonth() + i);

      repayments.push({
        loan: loanId,
        installmentNumber: i,
        dueDate: dueDate,
        amountDue: Math.round(emiAmount * 100) / 100,
        status: 'PENDING'
      });
    }

    await Repayment.insertMany(repayments);
    console.log(`Generated ${repayments.length} repayments for loan ${loanId}`);
  } catch (error) {
    console.error('Error generating repayments:', error);
    throw error;
  }
}