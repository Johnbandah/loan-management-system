const express = require('express');
const router = express.Router();
const reminderController = require('../controllers/reminderController');
const { authenticate } = require('../middleware/auth');

router.post('/send', authenticate, reminderController.sendPaymentReminders);
router.get('/customer/:customerId', authenticate, reminderController.getRemindersByCustomer);
router.put('/:id/read', authenticate, reminderController.markReminderRead);

module.exports = router;