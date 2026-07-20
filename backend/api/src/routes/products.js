const express = require('express');
const router = express.Router();
const productController = require('../controllers/productController');

// Public endpoints
router.get('/flash-sale', productController.getFlashSale);
router.get('/best-seller', productController.getBestSellers);
router.get('/search', productController.searchProducts);

// Python backend integration
router.post('/similar', productController.getSimilarProducts);

// Admin endpoints (Would typically be protected by an admin middleware)
router.get('/', productController.getAllProducts);
router.get('/:id', productController.getProductById);
router.post('/', productController.createProduct);
router.put('/:id', productController.updateProduct);
router.delete('/:id', productController.deleteProduct);

module.exports = router;
