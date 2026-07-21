const express = require('express');
const router = express.Router();
const orderController = require('../controllers/orderController');
const auth = require('../middlewares/auth');

// User endpoints
router.post('/checkout', auth, orderController.checkout);
router.get('/me', auth, orderController.getMyOrders);
router.patch('/me/:id/cancel', auth, orderController.cancelMyOrder);
router.get('/me/:id', auth, orderController.getMyOrderById);

// Admin endpoints
router.get('/', orderController.getAllOrders);
router.get('/:id', orderController.getOrderById);
router.put('/:id/status', orderController.updateOrderStatus);

module.exports = router;
