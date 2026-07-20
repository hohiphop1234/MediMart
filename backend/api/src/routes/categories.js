const express = require('express');
const router = express.Router();
const categoryController = require('../controllers/categoryController');

// Public endpoints
router.get('/', categoryController.getCategories);

// Admin endpoints
router.get('/:id', categoryController.getCategoryById);
router.post('/', categoryController.createCategory);
router.put('/:id', categoryController.updateCategory);
router.delete('/:id', categoryController.deleteCategory);

module.exports = router;
