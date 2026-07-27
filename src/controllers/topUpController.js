const TopUpRequest = require('../models/TopUpRequest');
const Loan = require('../models/Loan');
const Customer = require('../models/Customer');
const Notification = require('../models/Notification');

exports.requestTopUp = async (req, res) => {
  try {
    const { customerId, loanId, requestedAmount, proposedTenure, purpose } = req.body;

    const loan = await Loan.findById(loanId);
    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }

    const currentBalance = loan.remainingBalance || loan.totalPayable - (loan.amountPaid || 0);
    const maxTopUp = loan.loanAmount * 0.5; // Max 50% of original loan

    if (requestedAmount > maxTopUp) {
      return res.status(400).json({
        success: false,
        message: `Top-up amount exceeds maximum allowed (MWK ${maxTopUp.toLocaleString()})`
      });
    }

    const topUp = await TopUpRequest.create({
      customer: customerId,
      loan: loanId,
      requestedAmount,
      currentLoanBalance: currentBalance,
      proposedTenure,
      interestRate: loan.interestRate,
      purpose,
      status: 'PENDING',
      requestDate: new Date()
    });

    // Notify admin
    await Notification.create({
      title: 'New Top-Up Request',
      message: `Customer requested top-up of MWK ${requestedAmount.toLocaleString()} for Loan #${loanId}`,
      type: 'INFO',
      isAdmin: true
    });

    res.status(201).json({
      success: true,
      message: 'Top-up request submitted',
      topUp
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getTopUpRequests = async (req, res) => {
  try {
    const topUps = await TopUpRequest.find()
      .populate('customer', 'fullName email phone')
      .populate('loan', 'loanAmount status')
      .sort({ requestDate: -1 });
    res.json(topUps);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getTopUpRequestsByCustomer = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const topUps = await TopUpRequest.find({ customer: customerId })
      .populate('loan', 'loanAmount status')
      .sort({ requestDate: -1 });
    res.json(topUps);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.reviewTopUp = async (req, res) => {
  try {
    const topUpId = req.params.id;
    const { status, rejectionReason, reviewedBy } = req.body;

    const topUp = await TopUpRequest.findById(topUpId);
    if (!topUp) {
      return res.status(404).json({ success: false, message: 'Top-up request not found' });
    }

    topUp.status = status;
    topUp.reviewedBy = reviewedBy || 'Admin';
    topUp.reviewedAt = new Date();
    if (status === 'REJECTED') {
      topUp.rejectionReason = rejectionReason;
    }
    await topUp.save();

    // Notify customer
    await Notification.create({
      customer: topUp.customer,
      title: status === 'APPROVED' ? 'Top-Up Approved' : 'Top-Up Rejected',
      message: status === 'APPROVED'
        ? `Your top-up request for MWK ${topUp.requestedAmount.toLocaleString()} has been approved`
        : `Your top-up request was rejected: ${rejectionReason || 'No reason provided'}`,
      type: status === 'APPROVED' ? 'SUCCESS' : 'WARNING'
    });

    res.json({
      success: true,
      message: `Top-up ${status.toLowerCase()}`,
      topUp
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.disburseTopUp = async (req, res) => {
  try {
    const topUpId = req.params.id;
    const { disbursementMethod, disbursementReference } = req.body;

    const topUp = await TopUpRequest.findById(topUpId);
    if (!topUp) {
      return res.status(404).json({ success: false, message: 'Top-up request not found' });
    }

    if (topUp.status !== 'APPROVED') {
      return res.status(400).json({ success: false, message: 'Top-up must be approved first' });
    }

    topUp.status = 'DISBURSED';
    topUp.disbursedAmount = topUp.requestedAmount;
    topUp.disbursedAt = new Date();
    topUp.disbursementMethod = disbursementMethod;
    topUp.disbursementReference = disbursementReference;
    await topUp.save();

    // Update loan
    const loan = await Loan.findById(topUp.loan);
    if (loan) {
      loan.loanAmount += topUp.requestedAmount;
      loan.totalPayable += topUp.requestedAmount * (1 + loan.interestRate / 100);
      loan.remainingBalance += topUp.requestedAmount;
      await loan.save();

      // Generate new repayment schedule
      await generateRepayments(loan._id);
    }

    // Notify customer
    await Notification.create({
      customer: topUp.customer,
      title: 'Top-Up Disbursed',
      message: `Your top-up of MWK ${topUp.requestedAmount.toLocaleString()} has been disbursed`,
      type: 'SUCCESS'
    });

    res.json({
      success: true,
      message: 'Top-up disbursed successfully',
      topUp
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};