const express = require('express');
const router = express.Router();
const productController = require('../controllers/productController');

router.get('/flash-sale', productController.getFlashSale);
router.get('/best-seller', productController.getBestSellers);
router.get('/search', productController.searchProducts);

module.exports = router;
