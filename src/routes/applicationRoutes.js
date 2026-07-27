const express = require('express');
const router = express.Router();
const applicationController = require('../controllers/applicationController');
const { authenticate } = require('../middleware/auth');

router.get('/', authenticate, applicationController.getAllApplications);
router.get('/my-applications/:customerId', authenticate, applicationController.getMyApplications);
router.post('/apply/:customerId', authenticate, applicationController.applyForLoan);
router.put('/:applicationId/review', authenticate, applicationController.reviewApplication);

module.exports = router;