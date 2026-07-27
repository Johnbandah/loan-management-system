const express = require('express');
const router = express.Router();
const eligibilityController = require('../controllers/eligibilityController');
const { authenticate } = require('../middleware/auth');

router.post('/check/:customerId', authenticate, eligibilityController.checkEligibility);
router.get('/credit-score/:customerId', authenticate, eligibilityController.getCreditScore);

module.exports = router;