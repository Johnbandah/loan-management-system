const Customer = require('../models/Customer');

exports.getAllCustomers = async (req, res) => {
  try {
    const customers = await Customer.find().select('-password');
    res.json(customers);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getCustomerById = async (req, res) => {
  try {
    const customer = await Customer.findById(req.params.id).select('-password');
    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }
    res.json(customer);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.createWithAccount = async (req, res) => {
  try {
    const {
      fullName, email, phone, username, password,
      nationalId, address, creditScore,
      bankName, bankAccountNumber, bankAccountName, mobileMoneyNumber
    } = req.body;

    const existingCustomer = await Customer.findOne({
      $or: [{ email }, { username }, { phone }]
    });

    if (existingCustomer) {
      return res.status(400).json({
        success: false,
        message: 'Customer with this email, username, or phone already exists'
      });
    }

    const customer = await Customer.create({
      fullName,
      email,
      phone,
      username,
      password,
      nationalId,
      address,
      creditScore: creditScore || 0,
      bankName,
      bankAccountNumber,
      bankAccountName,
      mobileMoneyNumber
    });

    const customerResponse = customer.toObject();
    delete customerResponse.password;

    res.status(201).json({
      success: true,
      message: 'Customer created successfully',
      customer: customerResponse,
      username: customer.username
    });
  } catch (error) {
    console.error('Create customer error:', error);
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.updateProfile = async (req, res) => {
  try {
    const { fullName, email, phone, address } = req.body;
    const customerId = req.params.id;

    const customer = await Customer.findByIdAndUpdate(
      customerId,
      { fullName, email, phone, address },
      { new: true, runValidators: true }
    ).select('-password');

    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }

    res.json({
      success: true,
      message: 'Profile updated successfully',
      customer
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.changePassword = async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;
    const customerId = req.params.id;

    const customer = await Customer.findById(customerId);
    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }

    const isMatch = await customer.comparePassword(oldPassword);
    if (!isMatch) {
      return res.status(400).json({ success: false, message: 'Current password is incorrect' });
    }

    customer.password = newPassword;
    await customer.save();

    res.json({ success: true, message: 'Password changed successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.deleteCustomer = async (req, res) => {
  try {
    const customer = await Customer.findByIdAndDelete(req.params.id);
    if (!customer) {
      return res.status(404).json({ success: false, message: 'Customer not found' });
    }
    res.json({ success: true, message: 'Customer deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};