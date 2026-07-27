const express = require('express');
const router = express.Router();
const analyticsController = require('../controllers/analyticsController');
const { authenticate } = require('../middleware/auth');

router.get('/collection-summary', authenticate, analyticsController.getCollectionSummary);
router.get('/loan-distribution', authenticate, analyticsController.getLoanDistribution);
router.get('/status-breakdown', authenticate, analyticsController.getStatusBreakdown);
router.get('/monthly-loans', authenticate, analyticsController.getMonthlyLoans);
router.get('/recent-activities', authenticate, analyticsController.getRecentActivities);

module.exports = router;