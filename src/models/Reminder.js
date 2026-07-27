const mongoose = require('mongoose');

const reminderSchema = new mongoose.Schema({
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
  type: {
    type: String,
    enum: ['PAYMENT_DUE', 'PAYMENT_OVERDUE', 'PAYMENT_SUCCESS', 'LOAN_APPROVED', 
           'LOAN_DISBURSED', 'DOCUMENT_REMINDER', 'KYC_REMINDER'],
    required: true
  },
  message: {
    type: String,
    required: true,
    trim: true
  },
  priority: {
    type: String,
    enum: ['LOW', 'MEDIUM', 'HIGH', 'URGENT'],
    default: 'MEDIUM'
  },
  sentAt: {
    type: Date,
    default: Date.now
  },
  sentVia: {
    type: [String],
    enum: ['EMAIL', 'SMS', 'APP'],
    default: ['EMAIL']
  },
  readAt: {
    type: Date
  },
  delivered: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    trim: true
  },
  scheduledFor: {
    type: Date
  },
  completed: {
    type: Boolean,
    default: false
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Reminder', reminderSchema);