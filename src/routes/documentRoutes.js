const express = require('express');
const router = express.Router();
const documentController = require('../controllers/documentController');
const { authenticate } = require('../middleware/auth');
const multer = require('multer');

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, 'uploads/'),
  filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});
const upload = multer({ storage });

router.post('/upload', authenticate, upload.single('file'), documentController.uploadDocument);
router.get('/customer/:customerId', authenticate, documentController.getDocumentsByCustomer);
router.get('/:id', authenticate, documentController.getDocumentById);
router.put('/:id/verify', authenticate, documentController.verifyDocument);
router.delete('/:id', authenticate, documentController.deleteDocument);

module.exports = router;