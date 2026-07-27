const Customer = require('../models/Customer');
const Loan = require('../models/Loan');
const Payment = require('../models/Payment');
const Repayment = require('../models/Repayment');

exports.exportCustomers = async (req, res) => {
  try {
    const customers = await Customer.find();
    
    const csv = generateCustomerCSV(customers);
    
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', `attachment; filename=customers_${Date.now()}.csv`);
    res.send(csv);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.exportLoans = async (req, res) => {
  try {
    const loans = await Loan.find().populate('customer', 'fullName email phone');
    
    const csv = generateLoanCSV(loans);
    
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', `attachment; filename=loans_${Date.now()}.csv`);
    res.send(csv);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.exportPayments = async (req, res) => {
  try {
    const { loanId } = req.params;
    const query = loanId ? { loan: loanId } : {};
    
    const payments = await Payment.find(query)
      .populate('customer', 'fullName email')
      .populate('loan', 'loanAmount');
    
    const csv = generatePaymentCSV(payments);
    
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', `attachment; filename=payments_${Date.now()}.csv`);
    res.send(csv);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// CSV Generation Helpers
function generateCustomerCSV(customers) {
  const headers = ['ID', 'Name', 'Email', 'Phone', 'Credit Score', 'KYC Status', 'Registered Date'];
  const rows = customers.map(c => [
    c._id,
    c.fullName,
    c.email,
    c.phone,
    c.creditScore || 0,
    c.kycStatus || 'PENDING',
    c.createdAt.toISOString().split('T')[0]
  ]);
  return [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
}

function generateLoanCSV(loans) {
  const headers = ['ID', 'Customer', 'Amount', 'Tenure', 'EMI', 'Status', 'Disbursed', 'Remaining'];
  const rows = loans.map(l => [
    l._id,
    l.customer?.fullName || 'N/A',
    l.loanAmount,
    l.tenureMonths,
    l.emiAmount,
    l.status,
    l.amountDisbursed || 0,
    l.remainingBalance || 0
  ]);
  return [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
}

function generatePaymentCSV(payments) {
  const headers = ['Transaction ID', 'Customer', 'Loan', 'Amount', 'Method', 'Status', 'Date'];
  const rows = payments.map(p => [
    p.transactionId,
    p.customer?.fullName || 'N/A',
    p.loan?._id || 'N/A',
    p.amount,
    p.paymentMethod,
    p.status,
    p.requestedAt.toISOString().split('T')[0]
  ]);
  return [headers.join(','), ...rows.map(r => r.join(','))].join('\n');
}