const Notification = require('../models/Notification');

exports.getAdminNotifications = async (req, res) => {
  try {
    const notifications = await Notification.find({ isAdmin: true })
      .sort({ createdAt: -1 })
      .limit(50);
    res.json(notifications);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getAdminUnreadCount = async (req, res) => {
  try {
    const count = await Notification.countDocuments({ isAdmin: true, status: 'UNREAD' });
    res.json({ unreadCount: count });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.markAdminAsRead = async (req, res) => {
  try {
    const notification = await Notification.findByIdAndUpdate(
      req.params.notificationId,
      { status: 'READ' },
      { new: true }
    );
    if (!notification) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.markAllAdminAsRead = async (req, res) => {
  try {
    await Notification.updateMany(
      { isAdmin: true, status: 'UNREAD' },
      { status: 'READ' }
    );
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getCustomerNotifications = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const notifications = await Notification.find({ 
      customer: customerId,
      isAdmin: false
    }).sort({ createdAt: -1 });
    res.json(notifications);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getCustomerUnreadCount = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const count = await Notification.countDocuments({ 
      customer: customerId, 
      status: 'UNREAD',
      isAdmin: false
    });
    res.json({ unreadCount: count });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.markAsRead = async (req, res) => {
  try {
    const notification = await Notification.findByIdAndUpdate(
      req.params.notificationId,
      { status: 'READ' },
      { new: true }
    );
    if (!notification) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.markAllAsRead = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    await Notification.updateMany(
      { customer: customerId, status: 'UNREAD' },
      { status: 'READ' }
    );
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};