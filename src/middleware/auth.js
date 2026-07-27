const jwt = require('jsonwebtoken');
const Customer = require('../models/Customer');
const User = require('../models/User');

exports.authenticate = async (req, res, next) => {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    
    if (!token) {
      return res.status(401).json({ 
        success: false, 
        message: 'Authentication required' 
      });
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');
    
    let user = await Customer.findById(decoded.id);
    if (user) {
      req.user = user;
      req.userRole = 'CUSTOMER';
      return next();
    }

    user = await User.findById(decoded.id);
    if (user) {
      req.user = user;
      req.userRole = user.role;
      return next();
    }

    return res.status(401).json({ 
      success: false, 
      message: 'Invalid token' 
    });
  } catch (error) {
    return res.status(401).json({ 
      success: false, 
      message: 'Invalid or expired token' 
    });
  }
};

exports.authorize = (...roles) => {
  return (req, res, next) => {
    if (!req.userRole) {
      return res.status(401).json({ 
        success: false, 
        message: 'Unauthorized' 
      });
    }
    if (!roles.includes(req.userRole) && req.userRole !== 'CUSTOMER') {
      return res.status(403).json({ 
        success: false, 
        message: 'Insufficient permissions' 
      });
    }
    next();
  };
};