const mongoose = require('mongoose');

const documentSchema = new mongoose.Schema({
  customer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Customer',
    required: true
  },
  loanApplication: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'LoanApplication'
  },
  documentType: {
    type: String,
    enum: ['NATIONAL_ID', 'PASSPORT', 'DRIVERS_LICENSE', 'UTILITY_BILL', 
           'BANK_STATEMENT', 'PAYSLIP', 'EMPLOYMENT_LETTER', 'TAX_CERTIFICATE',
           'PROOF_OF_RESIDENCE', 'BUSINESS_REGISTRATION', 'FINANCIAL_STATEMENT',
           'OTHER'],
    required: true
  },
  fileName: {
    type: String,
    required: true,
    trim: true
  },
  fileUrl: {
    type: String,
    required: true
  },
  fileSize: {
    type: Number
  },
  mimeType: {
    type: String
  },
  description: {
    type: String,
    trim: true
  },
  status: {
    type: String,
    enum: ['PENDING', 'VERIFIED', 'REJECTED'],
    default: 'PENDING'
  },
  verifiedBy: {
    type: String,
    trim: true
  },
  verificationDate: {
    type: Date
  },
  rejectionReason: {
    type: String,
    trim: true
  },
  uploadedAt: {
    type: Date,
    default: Date.now
  },
  expiresAt: {
    type: Date
  }
}, {
  timestamps: true
});

module.exports = mongoose.model('Document', documentSchema);