const mongoose = require('mongoose');

const loanApplicationSchema = new mongoose.Schema({
  customer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Customer',
    required: true
  },
  requestedAmount: {
    type: Number,
    required: true
  },
  tenureMonths: {
    type: Number,
    required: true
  },
  interestRate: {
    type: Number,
    default: 12.5
  },
  loanType: {
    type: String,
    enum: ['PERSONAL', 'HOME', 'AUTO', 'EDUCATION', 'BUSINESS'],
    required: true
  },
  purpose: {
    type: String,
    required: true
  },
  monthlyIncome: {
    type: Number,
    required: true
  },
  employmentType: {
    type: String,
    enum: ['SALARIED', 'SELF_EMPLOYED', 'BUSINESS', 'OTHER'],
    required: true
  },
  employerName: {
    type: String,
    trim: true
  },
  status: {
    type: String,
    enum: ['PENDING', 'APPROVED', 'REJECTED'],
    default: 'PENDING'
  },
  rejectionReason: {
    type: String,
    trim: true
  },
  reviewedBy: {
    type: String,
    trim: true
  },
  applicationDate: {
    type: Date,
    default: Date.now
  },
  reviewedAt: {
    type: Date
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('LoanApplication', loanApplicationSchema);