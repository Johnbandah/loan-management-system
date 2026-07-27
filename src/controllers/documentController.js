const Document = require('../models/Document');
const Customer = require('../models/Customer');
const Notification = require('../models/Notification');

exports.uploadDocument = async (req, res) => {
  try {
    const { customerId, documentType, description, loanApplicationId } = req.body;
    const file = req.file;

    if (!file) {
      return res.status(400).json({ success: false, message: 'No file uploaded' });
    }

    const document = await Document.create({
      customer: customerId,
      loanApplication: loanApplicationId || null,
      documentType,
      fileName: file.originalname,
      fileUrl: `/uploads/${file.filename}`,
      fileSize: file.size,
      mimeType: file.mimetype,
      description,
      status: 'PENDING',
      uploadedAt: new Date()
    });

    // Notify admin
    await Notification.create({
      title: 'New Document Uploaded',
      message: `Customer uploaded ${documentType}: ${file.originalname}`,
      type: 'INFO',
      isAdmin: true
    });

    res.status(201).json({
      success: true,
      message: 'Document uploaded successfully',
      document
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getDocumentsByCustomer = async (req, res) => {
  try {
    const customerId = req.params.customerId;
    const documents = await Document.find({ customer: customerId })
      .sort({ uploadedAt: -1 });
    res.json(documents);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.getDocumentById = async (req, res) => {
  try {
    const document = await Document.findById(req.params.id);
    if (!document) {
      return res.status(404).json({ success: false, message: 'Document not found' });
    }
    res.json(document);
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.verifyDocument = async (req, res) => {
  try {
    const documentId = req.params.id;
    const { status, rejectionReason, verifiedBy } = req.body;

    const document = await Document.findByIdAndUpdate(
      documentId,
      {
        status,
        rejectionReason: status === 'REJECTED' ? rejectionReason : null,
        verifiedBy: verifiedBy || 'Admin',
        verificationDate: new Date()
      },
      { new: true }
    );

    if (!document) {
      return res.status(404).json({ success: false, message: 'Document not found' });
    }

    // Notify customer
    await Notification.create({
      customer: document.customer,
      title: status === 'VERIFIED' ? 'Document Verified' : 'Document Rejected',
      message: status === 'VERIFIED' 
        ? `Your ${document.documentType} has been verified`
        : `Your ${document.documentType} was rejected: ${rejectionReason}`,
      type: status === 'VERIFIED' ? 'SUCCESS' : 'WARNING'
    });

    res.json({
      success: true,
      message: `Document ${status.toLowerCase()}`,
      document
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.deleteDocument = async (req, res) => {
  try {
    const document = await Document.findByIdAndDelete(req.params.id);
    if (!document) {
      return res.status(404).json({ success: false, message: 'Document not found' });
    }
    res.json({ success: true, message: 'Document deleted' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};