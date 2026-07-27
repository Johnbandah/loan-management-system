const mongoose = require('mongoose');

const performanceLogSchema = new mongoose.Schema({
  metric: {
    type: String,
    enum: ['LOAN_APPLICATION', 'LOAN_APPROVAL', 'LOAN_DISBURSEMENT', 
           'PAYMENT', 'CUSTOMER_REGISTRATION', 'DOCUMENT_UPLOAD'],
    required: true
  },
  value: {
    type: Number,
    required: true
  },
  timeframe: {
    type: String,
    enum: ['DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY'],
    required: true
  },
  date: {
    type: Date,
    required: true
  },
  processedBy: {
    type: String,
    trim: true
  },
  metadata: {
    type: mongoose.Schema.Types.Mixed
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('PerformanceLog', performanceLogSchema);