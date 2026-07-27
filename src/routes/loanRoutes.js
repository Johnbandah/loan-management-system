const express = require('express');
const router = express.Router();
const loanController = require('../controllers/loanController');
const { authenticate } = require('../middleware/auth');

router.get('/', authenticate, loanController.getAllLoans);
router.get('/:loanId', authenticate, loanController.getLoanById);
router.get('/customer/:customerId', authenticate, loanController.getLoansByCustomer);
router.post('/apply/:customerId', authenticate, loanController.applyLoan);
router.put('/:loanId/approve', authenticate, loanController.approveLoan);
router.put('/:loanId/disburse', authenticate, loanController.disburseLoan);
router.get('/calculate-emi', authenticate, loanController.calculateEMI);

module.exports = router;