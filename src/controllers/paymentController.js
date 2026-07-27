const Payment = require('../models/Payment');
const Repayment = require('../models/Repayment');
const Loan = require('../models/Loan');
const Notification = require('../models/Notification');

exports.requestPayment = async (req, res) => {
  try {
    const { customerId, loanId, installmentNumber, mobileNumber, provider } = req.body;

    const repayment = await Repayment.findOne({ loan: loanId, installmentNumber });
    if (!repayment) {
      return res.status(404).json({ success: false, message: 'Repayment not found' });
    }

    if (repayment.status === 'PAID') {
      return res.status(400).json({ success: false, message: 'This installment is already paid' });
    }

    const payment = await Payment.create({
      customer: customerId,
      loan: loanId,
      repayment: repayment._id,
      amount: repayment.amountDue,
      paymentMethod: 'MOBILE_MONEY',
      provider,
      mobileNumber,
      status: 'PENDING',
      requestedAt: new Date()
    });

    // Simulate payment processing
    setTimeout(async () => {
      payment.status = 'COMPLETED';
      payment.completedAt = new Date();
      await payment.save();

      repayment.status = 'PAID';
      repayment.amountPaid = repayment.amountDue;
      repayment.paidDate = new Date();
      repayment.paymentMethod = 'MOBILE_MONEY';
      repayment.transactionReference = payment.transactionId;
      await repayment.save();

      const loan = await Loan.findById(loanId);
      if (loan) {
        loan.amountPaid = (loan.amountPaid || 0) + repayment.amountDue;
        loan.remainingBalance = loan.totalPayable - loan.amountPaid;

        const allRepayments = await Repayment.find({ loan: loanId });
        const allPaid = allRepayments.every(r => r.status === 'PAID');
        if (allPaid) {
          loan.status = 'CLOSED';
        }
        await loan.save();
      }

      await Notification.create({
        customer: customerId,
        title: 'Payment Successful',
        message: `Your payment of MWK ${repayment.amountDue.toLocaleString()} for Loan #${loanId} was successful.`,
        type: 'SUCCESS'
      });
    }, 2000);

    res.json({
      success: true,
      message: 'Payment request initiated successfully',
      transactionId: payment.transactionId,
      amount: payment.amount
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getPaymentHistory = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const payments = await Payment.find({ customer: customerId })
      .sort({ requestedAt: -1 })
      .populate('loan', 'loanAmount tenureMonths');
    res.json(payments);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getPaymentStatus = async (req, res) => {
  try {
    const transactionId = req.params.transactionId;
    const payment = await Payment.findOne({ transactionId });
    if (!payment) {
      return res.status(404).json({ success: false, message: 'Payment not found' });
    }
    res.json({
      transactionId: payment.transactionId,
      status: payment.status,
      amount: payment.amount,
      requestedAt: payment.requestedAt,
      completedAt: payment.completedAt
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};