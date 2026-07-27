const express = require('express');
const router = express.Router();
const exportController = require('../controllers/exportController');
const { authenticate } = require('../middleware/auth');

router.get('/customers', authenticate, exportController.exportCustomers);
router.get('/loans', authenticate, exportController.exportLoans);
router.get('/payments/:loanId?', authenticate, exportController.exportPayments);

module.exports = router;