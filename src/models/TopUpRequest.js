const mongoose = require('mongoose');

const topUpRequestSchema = new mongoose.Schema({
  customer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Customer',
    required: true
  },
  loan: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Loan',
    required: true
  },
  requestedAmount: {
    type: Number,
    required: true
  },
  currentLoanBalance: {
    type: Number,
    required: true
  },
  proposedTenure: {
    type: Number,
    required: true
  },
  interestRate: {
    type: Number,
    default: 12.5
  },
  purpose: {
    type: String,
    trim: true
  },
  status: {
    type: String,
    enum: ['PENDING', 'APPROVED', 'REJECTED', 'DISBURSED'],
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
  reviewedAt: {
    type: Date
  },
  disbursedAmount: {
    type: Number,
    default: 0
  },
  disbursedAt: {
    type: Date
  },
  disbursementMethod: {
    type: String,
    enum: ['BANK_TRANSFER', 'MOBILE_MONEY', 'CASH', 'CHEQUE']
  },
  disbursementReference: {
    type: String,
    trim: true
  },
  requestDate: {
    type: Date,
    default: Date.now
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('TopUpRequest', topUpRequestSchema);