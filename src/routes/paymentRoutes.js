const express = require('express');
const router = express.Router();
const paymentController = require('../controllers/paymentController');
const { authenticate } = require('../middleware/auth');

router.post('/request', authenticate, paymentController.requestPayment);
router.get('/history/:customerId', authenticate, paymentController.getPaymentHistory);
router.get('/status/:transactionId', authenticate, paymentController.getPaymentStatus);

module.exports = router;