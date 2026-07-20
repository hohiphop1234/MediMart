const orderService = require('../services/orderService');

function serializeOrder(order) {
    return {
        _id: order.id,
        totalAmount: Number(order.total_amount),
        status: order.status,
        paymentMethod: order.payment_method,
        createdAt: order.created_at
    };
}

exports.checkout = async (req, res) => {
    try {
        const { items, addressId, paymentMethod = 'COD' } = req.body;
        if (!Array.isArray(items) || !items.length || !addressId) {
            return res.status(400).json({ error: 'Cart items and an address are required.' });
        }

        const data = await orderService.checkout(items, addressId, paymentMethod, req.accessToken);
        
        res.status(201).json({
            orderId: data.order_id,
            totalAmount: Number(data.total_amount),
            earnedPoints: data.earned_points,
            status: data.status
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMyOrders = async (req, res) => {
    try {
        const status = typeof req.query.status === 'string' ? req.query.status : '';
        const orders = await orderService.getMyOrders(req.user.userId, status);
        res.json(orders.map(serializeOrder));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// --- Admin APIs ---

exports.getAllOrders = async (req, res) => {
    try {
        const orders = await orderService.getAllOrders();
        res.json(orders);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getOrderById = async (req, res) => {
    try {
        const order = await orderService.getOrderById(req.params.id);
        if (!order) return res.status(404).json({ error: 'Order not found' });
        res.json(order);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateOrderStatus = async (req, res) => {
    try {
        const { status } = req.body;
        if (!status) return res.status(400).json({ error: 'Status is required' });
        const updatedOrder = await orderService.updateOrderStatus(req.params.id, status);
        res.json(updatedOrder);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
