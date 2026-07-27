const Repayment = require('../models/Repayment');
const Loan = require('../models/Loan');
const Notification = require('../models/Notification');

exports.generateRepayments = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    const loan = await Loan.findById(loanId);
    
    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }

    const existing = await Repayment.findOne({ loan: loanId });
    if (existing) {
      return res.json({ success: true, message: 'Repayments already generated' });
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
        amountDue: emiAmount,
        status: 'PENDING'
      });
    }

    await Repayment.insertMany(repayments);

    res.json({
      success: true,
      message: `Generated ${repayments.length} repayment schedules`
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getRepaymentHistory = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    const repayments = await Repayment.find({ loan: loanId }).sort({ installmentNumber: 1 });
    res.json(repayments);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getRepaymentSummary = async (req, res) => {
  try {
    const loanId = req.params.loanId;
    const loan = await Loan.findById(loanId).populate('customer', 'fullName');
    const repayments = await Repayment.find({ loan: loanId });

    if (!loan) {
      return res.status(404).json({ success: false, message: 'Loan not found' });
    }

    const paidInstallments = repayments.filter(r => r.status === 'PAID').length;
    const totalInstallments = repayments.length;
    const amountPaid = repayments
      .filter(r => r.status === 'PAID')
      .reduce((sum, r) => sum + r.amountPaid, 0);
    const totalLoan = loan.loanAmount;
    const remainingBalance = totalLoan - amountPaid;
    const progressPercent = totalLoan > 0 ? (amountPaid / totalLoan) * 100 : 0;

    res.json({
      totalLoan,
      amountPaid,
      remainingBalance,
      progressPercent,
      emiAmount: loan.emiAmount,
      customerName: loan.customer?.fullName || 'N/A',
      paidInstallments,
      totalInstallments,
      status: loan.status
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.recordPayment = async (req, res) => {
  try {
    const { loanId, installmentNumber, amountPaid, paymentMethod, transactionReference } = req.body;

    const repayment = await Repayment.findOne({ loan: loanId, installmentNumber });
    if (!repayment) {
      return res.status(404).json({ success: false, message: 'Repayment not found' });
    }

    if (repayment.status === 'PAID') {
      return res.status(400).json({ success: false, message: 'This installment is already paid' });
    }

    repayment.status = 'PAID';
    repayment.amountPaid = amountPaid;
    repayment.paidDate = new Date();
    repayment.paymentMethod = paymentMethod;
    repayment.transactionReference = transactionReference;
    await repayment.save();

    const loan = await Loan.findById(loanId);
    if (loan) {
      loan.amountPaid = (loan.amountPaid || 0) + amountPaid;
      loan.remainingBalance = loan.totalPayable - loan.amountPaid;

      const allRepayments = await Repayment.find({ loan: loanId });
      const allPaid = allRepayments.every(r => r.status === 'PAID');
      if (allPaid) {
        loan.status = 'CLOSED';
      }

      await loan.save();
    }

    await Notification.create({
      customer: loan.customer,
      title: 'Payment Received',
      message: `Payment of MWK ${amountPaid.toLocaleString()} for Loan #${loanId} has been recorded.`,
      type: 'SUCCESS'
    });

    res.json({ success: true, message: 'Payment recorded successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};