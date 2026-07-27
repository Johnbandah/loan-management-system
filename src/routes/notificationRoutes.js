const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');
const { authenticate } = require('../middleware/auth');

// Admin notifications
router.get('/admin', authenticate, notificationController.getAdminNotifications);
router.get('/admin/unread', authenticate, notificationController.getAdminUnreadCount);
router.put('/admin/read/:notificationId', authenticate, notificationController.markAdminAsRead);
router.put('/admin/read-all', authenticate, notificationController.markAllAdminAsRead);

// Customer notifications
router.get('/customer/:customerId', authenticate, notificationController.getCustomerNotifications);
router.get('/unread/:customerId', authenticate, notificationController.getCustomerUnreadCount);
router.put('/read/:notificationId', authenticate, notificationController.markAsRead);
router.put('/read-all/:customerId', authenticate, notificationController.markAllAsRead);

module.exports = router;