const mongoose = require('mongoose');

const loanSchema = new mongoose.Schema({
  customer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Customer',
    required: true
  },
  application: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'LoanApplication'
  },
  loanAmount: {
    type: Number,
    required: true
  },
  tenureMonths: {
    type: Number,
    required: true
  },
  interestRate: {
    type: Number,
    required: true,
    default: 12.5
  },
  emiAmount: {
    type: Number,
    required: true,
    default: 0
  },
  totalPayable: {
    type: Number,
    required: true,
    default: 0
  },
  amountDisbursed: {
    type: Number,
    default: 0
  },
  disbursementDate: {
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
  status: {
    type: String,
    enum: ['PENDING', 'APPROVED', 'ACTIVE', 'CLOSED', 'REJECTED'],
    default: 'PENDING'
  },
  amountPaid: {
    type: Number,
    default: 0
  },
  remainingBalance: {
    type: Number,
    default: 0
  }
}, {
  timestamps: true
});

// Pre-save middleware to calculate EMI and total payable
loanSchema.pre('save', function(next) {
  try {
    // Only calculate if this is a new loan or if loan amount/tenure/rate changed
    if (this.isNew || this.isModified('loanAmount') || this.isModified('tenureMonths') || this.isModified('interestRate')) {
      const P = this.loanAmount;
      const r = this.interestRate / 100 / 12;
      const n = this.tenureMonths;
      
      if (!P || !n) {
        console.warn('Missing loan amount or tenure for calculation');
        return next();
      }
      
      if (r === 0) {
        this.emiAmount = P / n;
      } else {
        this.emiAmount = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
      }
      
      // Round to 2 decimal places
      this.emiAmount = Math.round(this.emiAmount * 100) / 100;
      this.totalPayable = Math.round((this.emiAmount * n) * 100) / 100;
      this.remainingBalance = this.totalPayable;
      
      console.log('Loan calculated:', {
        emiAmount: this.emiAmount,
        totalPayable: this.totalPayable,
        remainingBalance: this.remainingBalance
      });
    }
    next();
  } catch (error) {
    console.error('Error in loan pre-save:', error);
    next(error);
  }
});

module.exports = mongoose.model('Loan', loanSchema);