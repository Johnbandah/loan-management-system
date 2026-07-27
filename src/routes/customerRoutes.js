const express = require('express');
const router = express.Router();
const customerController = require('../controllers/customerController');
const { authenticate } = require('../middleware/auth');

router.post('/create-with-account', customerController.createWithAccount);
router.get('/', authenticate, customerController.getAllCustomers);
router.get('/:id', authenticate, customerController.getCustomerById);
router.put('/:id/update-profile', authenticate, customerController.updateProfile);
router.put('/:id/change-password', authenticate, customerController.changePassword);
router.delete('/:id', authenticate, customerController.deleteCustomer);

module.exports = router;