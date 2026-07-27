const Customer = require('../models/Customer');
const User = require('../models/User');
const jwt = require('jsonwebtoken');

// Generate JWT token
const generateToken = (id, role) => {
  return jwt.sign(
    { id, role },
    process.env.JWT_SECRET || 'your-secret-key',
    { expiresIn: process.env.JWT_EXPIRE || '7d' }
  );
};

// ============================================================
//  LOGIN
// ============================================================
exports.login = async (req, res) => {
  try {
    const { username, password } = req.body;

    console.log('Login attempt for:', username);

    if (!username || !password) {
      return res.status(400).json({ 
        success: false, 
        message: 'Username and password are required' 
      });
    }

    // Check customer login
    const customer = await Customer.findOne({ username });
    if (customer) {
      const isMatch = await customer.comparePassword(password);
      if (isMatch) {
        const token = generateToken(customer._id, 'CUSTOMER');
        return res.json({
          success: true,
          token,
          user: {
            id: customer._id,
            username: customer.username,
            email: customer.email,
            fullName: customer.fullName,
            role: 'CUSTOMER'
          }
        });
      }
    }

    // Check admin/staff login
    const user = await User.findOne({ username });
    if (!user) {
      return res.status(401).json({ 
        success: false, 
        message: 'Invalid username or password' 
      });
    }

    // Check if user is active
    if (!user.isActive) {
      return res.status(401).json({ 
        success: false, 
        message: 'Account is disabled. Please contact administrator.' 
      });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ 
        success: false, 
        message: 'Invalid username or password' 
      });
    }

    const token = generateToken(user._id, user.role);
    res.json({
      success: true,
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        fullName: user.fullName,
        role: user.role
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Server error. Please try again later.' 
    });
  }
};

// ============================================================
//  VERIFY TOKEN
// ============================================================
exports.verifyToken = async (req, res) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) {
      return res.json({ valid: false });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');
    
    // Check if user still exists
    let user = await Customer.findById(decoded.id);
    if (!user) {
      user = await User.findById(decoded.id);
    }

    if (!user) {
      return res.json({ valid: false });
    }

    res.json({ 
      valid: true, 
      user: { 
        id: user._id, 
        role: user.role || 'CUSTOMER' 
      } 
    });
  } catch (error) {
    res.json({ valid: false });
  }
};

// ============================================================
//  LOGOUT
// ============================================================
exports.logout = async (req, res) => {
  res.json({ 
    success: true, 
    message: 'Logged out successfully' 
  });
};

// ============================================================
//  GET CURRENT USER
// ============================================================
exports.getCurrentUser = async (req, res) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) {
      return res.json({ loggedIn: false });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');
    
    // Check if customer
    const customer = await Customer.findById(decoded.id);
    if (customer) {
      return res.json({
        loggedIn: true,
        customerId: customer._id,
        customerName: customer.fullName,
        email: customer.email,
        role: 'CUSTOMER'
      });
    }

    // Check if admin/staff
    const user = await User.findById(decoded.id);
    if (user) {
      return res.json({
        loggedIn: true,
        userId: user._id,
        userName: user.fullName,
        email: user.email,
        role: user.role
      });
    }

    res.json({ loggedIn: false });
  } catch (error) {
    res.json({ loggedIn: false });
  }
};