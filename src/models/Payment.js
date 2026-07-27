const mongoose = require('mongoose');

const paymentSchema = new mongoose.Schema({
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
  repayment: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Repayment'
  },
  amount: {
    type: Number,
    required: true
  },
  paymentMethod: {
    type: String,
    enum: ['CASH', 'BANK_TRANSFER', 'MOBILE_MONEY', 'CHEQUE'],
    required: true
  },
  provider: {
    type: String,
    enum: ['AIRTEL_MONEY', 'TNM_MPAMBA', 'BANK', 'CASH']
  },
  mobileNumber: {
    type: String,
    trim: true
  },
  transactionId: {
    type: String,
    unique: true,
    trim: true
  },
  transactionReference: {
    type: String,
    trim: true
  },
  status: {
    type: String,
    enum: ['PENDING', 'COMPLETED', 'FAILED'],
    default: 'PENDING'
  },
  requestedAt: {
    type: Date,
    default: Date.now
  },
  completedAt: {
    type: Date
  }
}, {
  timestamps: true
});

paymentSchema.pre('save', function(next) {
  if (this.isNew && !this.transactionId) {
    this.transactionId = 'TXN-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
  }
  next();
});

module.exports = mongoose.model('Payment', paymentSchema);