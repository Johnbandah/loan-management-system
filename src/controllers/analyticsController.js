const Loan = require('../models/Loan');
const LoanApplication = require('../models/LoanApplication');
const Payment = require('../models/Payment');
const Repayment = require('../models/Repayment');

exports.getCollectionSummary = async (req, res) => {
  try {
    const loans = await Loan.find();
    const repayments = await Repayment.find();

    const totalDisbursed = loans.reduce((sum, l) => sum + (l.amountDisbursed || 0), 0);
    const totalCollected = repayments
      .filter(r => r.status === 'PAID')
      .reduce((sum, r) => sum + r.amountPaid, 0);
    const totalPending = totalDisbursed - totalCollected;
    const collectionRate = totalDisbursed > 0 ? (totalCollected / totalDisbursed) * 100 : 0;

    res.json({
      totalDisbursed,
      totalCollected,
      totalPending,
      collectionRate: Math.round(collectionRate)
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getLoanDistribution = async (req, res) => {
  try {
    const applications = await LoanApplication.find();

    const distribution = applications.reduce((acc, app) => {
      acc[app.loanType] = (acc[app.loanType] || 0) + 1;
      return acc;
    }, {});

    res.json({
      labels: Object.keys(distribution),
      values: Object.values(distribution)
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getStatusBreakdown = async (req, res) => {
  try {
    const loans = await Loan.find();

    const breakdown = loans.reduce((acc, loan) => {
      acc[loan.status] = (acc[loan.status] || 0) + 1;
      return acc;
    }, {});

    res.json({
      labels: Object.keys(breakdown),
      values: Object.values(breakdown)
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getMonthlyLoans = async (req, res) => {
  try {
    const loans = await Loan.find().sort({ createdAt: 1 });

    const monthlyData = {};
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    loans.forEach(loan => {
      const date = new Date(loan.createdAt);
      const monthKey = months[date.getMonth()];
      const year = date.getFullYear();
      const key = `${monthKey} ${year}`;

      if (!monthlyData[key]) {
        monthlyData[key] = 0;
      }
      monthlyData[key] += loan.loanAmount || 0;
    });

    const now = new Date();
    const last12Months = [];
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now);
      d.setMonth(d.getMonth() - i);
      last12Months.push(`${months[d.getMonth()]} ${d.getFullYear()}`);
    }

    const amounts = last12Months.map(month => monthlyData[month] || 0);

    res.json({
      months: last12Months,
      amounts: amounts
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getRecentActivities = async (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 10;
    const activities = [];

    const payments = await Payment.find()
      .sort({ createdAt: -1 })
      .limit(limit)
      .populate('customer', 'fullName');

    payments.forEach(p => {
      activities.push({
        type: 'PAYMENT_RECEIVED',
        message: `Payment of MWK ${p.amount.toLocaleString()} received`,
        amount: p.amount,
        customer: p.customer?.fullName || 'Unknown',
        date: p.createdAt
      });
    });

    const applications = await LoanApplication.find()
      .sort({ createdAt: -1 })
      .limit(limit)
      .populate('customer', 'fullName');

    applications.forEach(app => {
      activities.push({
        type: app.status === 'APPROVED' ? 'LOAN_APPROVED' : 'LOAN_APPLIED',
        message: app.status === 'APPROVED' ? 
          `Loan application #${app._id} approved` : 
          `New loan application for MWK ${app.requestedAmount.toLocaleString()}`,
        amount: app.requestedAmount,
        customer: app.customer?.fullName || 'Unknown',
        date: app.createdAt
      });
    });

    activities.sort((a, b) => new Date(b.date) - new Date(a.date));
    const limitedActivities = activities.slice(0, limit);

    res.json(limitedActivities);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};