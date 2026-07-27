const nodemailer = require('nodemailer');

let transporter = null;

if (process.env.EMAIL_HOST) {
  transporter = nodemailer.createTransport({
    host: process.env.EMAIL_HOST,
    port: process.env.EMAIL_PORT || 587,
    secure: process.env.EMAIL_SECURE === 'true',
    auth: {
      user: process.env.EMAIL_USER,
      pass: process.env.EMAIL_PASS
    }
  });
}

exports.sendPaymentReminder = async (to, data) => {
  if (!transporter) {
    console.log('Email service not configured. Would send reminder to:', to);
    return;
  }

  const { customerName, loanId, amount, dueDate, daysUntil } = data;

  const mailOptions = {
    from: process.env.EMAIL_USER,
    to,
    subject: `Payment Reminder - Loan #${loanId}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #1e3c72;">Payment Reminder</h2>
        <p>Dear ${customerName},</p>
        <p>This is a reminder that your payment of <strong>MWK ${amount.toLocaleString()}</strong> for Loan #${loanId} is due on <strong>${new Date(dueDate).toLocaleDateString()}</strong>.</p>
        <p style="color: ${daysUntil <= 3 ? '#e74c3c' : '#f39c12'};">
          ${daysUntil <= 3 ? '⚠️ URGENT: Payment is due in ' + daysUntil + ' days!' : '📅 Payment is due in ' + daysUntil + ' days.'}
        </p>
        <p>Please ensure you make the payment on time to avoid penalties.</p>
        <hr>
        <p style="color: #7f8c8d; font-size: 12px;">This is an automated message from Loan Management System.</p>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    console.log('Reminder email sent to:', to);
    return true;
  } catch (error) {
    console.error('Email sending failed:', error);
    throw error;
  }
};

exports.sendLoanApproval = async (to, data) => {
  if (!transporter) {
    console.log('Email service not configured. Would send approval to:', to);
    return;
  }

  const { customerName, loanId, amount, emi } = data;

  const mailOptions = {
    from: process.env.EMAIL_USER,
    to,
    subject: `Loan Approved - #${loanId}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #27ae60;">Congratulations! Loan Approved</h2>
        <p>Dear ${customerName},</p>
        <p>We are pleased to inform you that your loan application #${loanId} has been <strong>approved</strong>.</p>
        <p><strong>Amount:</strong> MWK ${amount.toLocaleString()}</p>
        <p><strong>Monthly EMI:</strong> MWK ${emi.toLocaleString()}</p>
        <p>You will receive a disbursement confirmation shortly.</p>
        <hr>
        <p style="color: #7f8c8d; font-size: 12px;">This is an automated message from Loan Management System.</p>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    console.log('Approval email sent to:', to);
    return true;
  } catch (error) {
    console.error('Email sending failed:', error);
    throw error;
  }
};

exports.sendDisbursementConfirmation = async (to, data) => {
  if (!transporter) {
    console.log('Email service not configured. Would send disbursement to:', to);
    return;
  }

  const { customerName, loanId, amount, method, reference } = data;

  const mailOptions = {
    from: process.env.EMAIL_USER,
    to,
    subject: `Loan Disbursed - #${loanId}`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #3498db;">Loan Disbursed</h2>
        <p>Dear ${customerName},</p>
        <p>Your loan #${loanId} has been <strong>disbursed</strong>.</p>
        <p><strong>Amount:</strong> MWK ${amount.toLocaleString()}</p>
        <p><strong>Method:</strong> ${method}</p>
        <p><strong>Reference:</strong> ${reference}</p>
        <p>Please check your account for the funds.</p>
        <hr>
        <p style="color: #7f8c8d; font-size: 12px;">This is an automated message from Loan Management System.</p>
      </div>
    `
  };

  try {
    await transporter.sendMail(mailOptions);
    console.log('Disbursement email sent to:', to);
    return true;
  } catch (error) {
    console.error('Email sending failed:', error);
    throw error;
  }
};