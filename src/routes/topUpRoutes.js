const express = require('express');
const router = express.Router();
const topUpController = require('../controllers/topUpController');
const { authenticate } = require('../middleware/auth');

router.post('/request', authenticate, topUpController.requestTopUp);
router.get('/', authenticate, topUpController.getTopUpRequests);
router.get('/customer/:customerId', authenticate, topUpController.getTopUpRequestsByCustomer);
router.put('/:id/review', authenticate, topUpController.reviewTopUp);
router.put('/:id/disburse', authenticate, topUpController.disburseTopUp);

module.exports = router;