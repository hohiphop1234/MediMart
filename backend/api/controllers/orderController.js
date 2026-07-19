const Order = require('../models/Order');
const Product = require('../models/Product');
const User = require('../models/User');

exports.checkout = async (req, res) => {
    try {
        const { items, addressId, paymentMethod } = req.body;
        if (!items || items.length === 0) return res.status(400).json({ error: 'Cart is empty' });

        let totalAmount = 0;
        const orderItems = [];

        for (let item of items) {
            const product = await Product.findById(item.productId);
            if (!product) return res.status(404).json({ error: `Product not found: ${item.productId}` });
            
            const currentPrice = product.salePrice || product.price;
            totalAmount += currentPrice * item.quantity;

            orderItems.push({
                productId: product._id,
                name: product.name,
                quantity: item.quantity,
                price: currentPrice
            });
        }

        const order = new Order({
            userId: req.user.userId,
            items: orderItems,
            totalAmount,
            addressId,
            paymentMethod: paymentMethod || 'COD',
            status: 'PENDING'
        });

        await order.save();

        const earnedPoints = Math.floor(totalAmount / 10000);
        await User.findByIdAndUpdate(req.user.userId, { $inc: { loyaltyPoints: earnedPoints } });

        res.json({ orderId: order._id, totalAmount, status: order.status });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMyOrders = async (req, res) => {
    try {
        const { status } = req.query;
        const query = { userId: req.user.userId };
        if (status) query.status = status;

        const orders = await Order.find(query).sort({ createdAt: -1 });
        res.json(orders);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
