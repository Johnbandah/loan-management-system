const Reminder = require('../models/Reminder');
const Repayment = require('../models/Repayment');
const Loan = require('../models/Loan');
const Customer = require('../models/Customer');
const Notification = require('../models/Notification');
const emailService = require('../services/emailService');

exports.sendPaymentReminders = async (req, res) => {
  try {
    const today = new Date();
    const threeDaysFromNow = new Date(today);
    threeDaysFromNow.setDate(today.getDate() + 3);
    const sevenDaysFromNow = new Date(today);
    sevenDaysFromNow.setDate(today.getDate() + 7);

    // Find pending repayments due in 3-7 days
    const pendingRepayments = await Repayment.find({
      status: 'PENDING',
      dueDate: {
        $gte: threeDaysFromNow,
        $lte: sevenDaysFromNow
      }
    }).populate('loan');

    const reminders = [];
    for (const repayment of pendingRepayments) {
      const loan = repayment.loan;
      const customer = await Customer.findById(loan.customer);
      
      if (!customer) continue;

      const daysUntil = Math.ceil((repayment.dueDate - today) / (1000 * 60 * 60 * 24));
      const priority = daysUntil <= 3 ? 'URGENT' : daysUntil <= 5 ? 'HIGH' : 'MEDIUM';

      // Create reminder record
      const reminder = await Reminder.create({
        customer: customer._id,
        loan: loan._id,
        repayment: repayment._id,
        type: 'PAYMENT_DUE',
        message: `Payment of MWK ${repayment.amountDue.toLocaleString()} for Loan #${loan._id} is due on ${new Date(repayment.dueDate).toLocaleDateString()}`,
        priority,
        sentAt: new Date(),
        sentVia: ['EMAIL'],
        scheduledFor: repayment.dueDate
      });

      // Send email
      try {
        await emailService.sendPaymentReminder(customer.email, {
          customerName: customer.fullName,
          loanId: loan._id,
          amount: repayment.amountDue,
          dueDate: repayment.dueDate,
          daysUntil
        });
        reminder.delivered = true;
      } catch (error) {
        reminder.error = error.message;
      }
      await reminder.save();

      // Create notification
      await Notification.create({
        customer: customer._id,
        title: 'Payment Reminder',
        message: `Your payment of MWK ${repayment.amountDue.toLocaleString()} is due in ${daysUntil} days`,
        type: daysUntil <= 3 ? 'URGENT' : 'WARNING'
      });

      reminders.push(reminder);
    }

    res.json({
      success: true,
      message: `Sent ${reminders.length} reminders`,
      reminders
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getRemindersByCustomer = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const reminders = await Reminder.find({ customer: customerId })
      .populate('loan', 'loanAmount status')
      .sort({ scheduledFor: 1 });
    res.json(reminders);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.markReminderRead = async (req, res) => {
  try {
    const reminderId = req.params.id;
    const reminder = await Reminder.findByIdAndUpdate(
      reminderId,
      { readAt: new Date() },
      { new: true }
    );
    if (!reminder) {
      return res.status(404).json({ success: false, message: 'Reminder not found' });
    }
    res.json({ success: true, reminder });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};