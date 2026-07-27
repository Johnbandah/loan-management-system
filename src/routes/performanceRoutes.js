const express = require('express');
const router = express.Router();
const performanceController = require('../controllers/performanceController');
const { authenticate } = require('../middleware/auth');

router.get('/metrics', authenticate, performanceController.getPerformanceMetrics);
router.post('/log', authenticate, performanceController.logPerformanceMetric);

module.exports = router;