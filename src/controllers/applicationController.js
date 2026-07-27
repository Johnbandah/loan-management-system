const LoanApplication = require('../models/LoanApplication');
const Loan = require('../models/Loan');
const Notification = require('../models/Notification');
const Repayment = require('../models/Repayment');

// ============================================================
//  GET ALL APPLICATIONS
// ============================================================
exports.getAllApplications = async (req, res) => {
  try {
    const applications = await LoanApplication.find()
      .populate('customer', 'fullName email phone')
      .sort({ applicationDate: -1 });
    res.json(applications);
  } catch (error) {
    console.error('Get all applications error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  GET MY APPLICATIONS
// ============================================================
exports.getMyApplications = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const applications = await LoanApplication.find({ customer: customerId })
      .sort({ applicationDate: -1 });
    res.json(applications);
  } catch (error) {
    console.error('Get my applications error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

// ============================================================
//  APPLY FOR LOAN
// ============================================================
exports.applyForLoan = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const { requestedAmount, tenureMonths, loanType, purpose, monthlyIncome, employmentType, employerName } = req.body;

    const application = await LoanApplication.create({
      customer: customerId,
      requestedAmount,
      tenureMonths,
      loanType,
      purpose,
      monthlyIncome,
      employmentType,
      employerName,
      status: 'PENDING',
      applicationDate: new Date()
    });

    await Notification.create({
      title: 'New Loan Application',
      message: `Customer applied for ${loanType} loan of MWK ${requestedAmount.toLocaleString()}`,
      type: 'INFO',
      isAdmin: true
    });

    res.status(201).json({
      success: true,
      message: 'Application submitted successfully',
      applicationId: application._id
    });
  } catch (error) {
    console.error('Apply for loan error:', error);
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

// ============================================================
//  REVIEW APPLICATION - FIXED - Auto approves loan
// ============================================================
exports.reviewApplication = async (req, res) => {
  try {
    const applicationId = req.params.applicationId;
    const { status, rejectionReason, reviewedBy } = req.body;

    console.log('Reviewing application:', applicationId, 'Status:', status);

    // Find the application
    const application = await LoanApplication.findById(applicationId).populate('customer');
    if (!application) {
      return res.status(404).json({ 
        success: false, 
        message: 'Application not found' 
      });
    }

    // Check if already reviewed
    if (application.status !== 'PENDING') {
      return res.status(400).json({ 
        success: false, 
        message: `Application already ${application.status.toLowerCase()}` 
      });
    }

    // Handle REJECTED
    if (status === 'REJECTED') {
      application.status = 'REJECTED';
      application.reviewedBy = reviewedBy || 'Admin';
      application.reviewedAt = new Date();
      application.rejectionReason = rejectionReason || 'No reason provided';
      await application.save();

      // Notify customer
      await Notification.create({
        customer: application.customer._id,
        title: 'Loan Application Rejected',
        message: `Your loan application for MWK ${application.requestedAmount.toLocaleString()} was rejected. Reason: ${application.rejectionReason}`,
        type: 'WARNING'
      });

      return res.json({
        success: true,
        message: 'Application rejected successfully',
        application
      });
    }

    // Handle APPROVED - Create loan and auto-approve it
    if (status === 'APPROVED') {
      // Update application status
      application.status = 'APPROVED';
      application.reviewedBy = reviewedBy || 'Admin';
      application.reviewedAt = new Date();
      await application.save();

      // Calculate EMI and total payable
      const P = application.requestedAmount;
      const annualRate = application.interestRate || 12.5;
      const r = annualRate / 100 / 12;
      const n = application.tenureMonths;
      
      let emiAmount;
      if (r === 0) {
        emiAmount = P / n;
      } else {
        emiAmount = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
      }
      
      const totalPayable = emiAmount * n;

      console.log('Creating loan with:', {
        customer: application.customer._id,
        loanAmount: P,
        tenureMonths: n,
        interestRate: annualRate,
        emiAmount: Math.round(emiAmount * 100) / 100,
        totalPayable: Math.round(totalPayable * 100) / 100
      });

      // Create loan with APPROVED status (not PENDING)
      const loan = await Loan.create({
        customer: application.customer._id,
        application: application._id,
        loanAmount: P,
        tenureMonths: n,
        interestRate: annualRate,
        emiAmount: Math.round(emiAmount * 100) / 100,
        totalPayable: Math.round(totalPayable * 100) / 100,
        status: 'APPROVED' // Auto-approve the loan
      });

      console.log('Loan created and approved:', loan._id);

      // Generate repayment schedule
      await generateRepayments(loan._id);

      // Notify customer
      await Notification.create({
        customer: application.customer._id,
        title: 'Loan Application Approved',
        message: `Your loan application for MWK ${application.requestedAmount.toLocaleString()} has been approved!`,
        type: 'SUCCESS'
      });

      // Notify admin
      await Notification.create({
        title: 'Loan Approved',
        message: `Loan #${loan._id} of MWK ${loan.loanAmount.toLocaleString()} has been approved`,
        type: 'SUCCESS',
        isAdmin: true
      });

      return res.json({
        success: true,
        message: 'Application approved and loan created successfully',
        application,
        loan
      });
    }

    return res.status(400).json({
      success: false,
      message: `Invalid status: ${status}`
    });

  } catch (error) {
    console.error('Review application error:', error);
    res.status(500).json({ 
      success: false, 
      message: error.message || 'Server error while reviewing application' 
    });
  }
};