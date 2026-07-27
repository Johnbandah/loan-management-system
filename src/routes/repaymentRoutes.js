const express = require('express');
const router = express.Router();
const repaymentController = require('../controllers/repaymentController');
const { authenticate } = require('../middleware/auth');

router.post('/generate/:loanId', authenticate, repaymentController.generateRepayments);
router.get('/history/:loanId', authenticate, repaymentController.getRepaymentHistory);
router.get('/summary/:loanId', authenticate, repaymentController.getRepaymentSummary);
router.post('/pay', authenticate, repaymentController.recordPayment);

module.exports = router;