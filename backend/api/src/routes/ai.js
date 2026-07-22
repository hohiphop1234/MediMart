const express = require('express');
const router = express.Router();
const aiController = require('../controllers/aiController');
const auth = require('../middlewares/auth');

// POST /api/ocr -> upload image, get medicine + quantity (Admin Web format)
router.post('/ocr', auth, aiController.uploadMiddleware, aiController.ocr);

// POST /api/ocr/prescription -> upload image, get List<Product> (Mobile App format)
router.post('/ocr/prescription', auth, aiController.uploadMiddleware, aiController.ocrPrescription);

// POST /api/chat -> send message, get AI response
router.post('/chat', auth, aiController.chat);

module.exports = router;
