const express = require('express');
const router = express.Router();
const orderController = require('../controllers/orderController');
const auth = require('../middleware/auth');

router.use(auth);

router.post('/checkout', orderController.checkout);
router.get('/my-orders', orderController.getMyOrders);

module.exports = router;
