const PerformanceLog = require('../models/PerformanceLog');
const Loan = require('../models/Loan');
const LoanApplication = require('../models/LoanApplication');
const Customer = require('../models/Customer');
const Payment = require('../models/Payment');

exports.getPerformanceMetrics = async (req, res) => {
  try {
    const { timeframe } = req.query;
    const startDate = new Date();
    
    switch(timeframe) {
      case 'DAILY':
        startDate.setDate(startDate.getDate() - 1);
        break;
      case 'WEEKLY':
        startDate.setDate(startDate.getDate() - 7);
        break;
      case 'MONTHLY':
        startDate.setMonth(startDate.getMonth() - 1);
        break;
      default:
        startDate.setMonth(startDate.getMonth() - 1);
    }

    // Aggregate metrics
    const metrics = await PerformanceLog.aggregate([
      { $match: { date: { $gte: startDate } } },
      { $group: {
        _id: '$metric',
        total: { $sum: '$value' }
      }}
    ]);

    // Calculate current metrics from database
    const [
      totalCustomers,
      totalApplications,
      approvedApplications,
      totalLoans,
      activeLoans,
      totalPayments
    ] = await Promise.all([
      Customer.countDocuments(),
      LoanApplication.countDocuments(),
      LoanApplication.countDocuments({ status: 'APPROVED' }),
      Loan.countDocuments(),
      Loan.countDocuments({ status: 'ACTIVE' }),
      Payment.countDocuments({ status: 'COMPLETED' })
    ]);

    res.json({
      success: true,
      metrics: {
        totalCustomers,
        totalApplications,
        approvalRate: totalApplications > 0 ? (approvedApplications / totalApplications * 100).toFixed(1) : 0,
        totalLoans,
        activeLoans,
        totalPayments,
        conversionRate: totalCustomers > 0 ? (totalLoans / totalCustomers * 100).toFixed(1) : 0,
        averageLoanSize: totalLoans > 0 ? (await Loan.aggregate([
          { $group: { _id: null, avg: { $avg: '$loanAmount' } } }
        ]))[0]?.avg || 0 : 0,
        historicalData: metrics
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.logPerformanceMetric = async (req, res) => {
  try {
    const { metric, value, timeframe, metadata } = req.body;
    
    const log = await PerformanceLog.create({
      metric,
      value,
      timeframe: timeframe || 'DAILY',
      date: new Date(),
      metadata,
      processedBy: req.user?.fullName || 'System'
    });

    res.status(201).json({
      success: true,
      message: 'Metric logged',
      log
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};