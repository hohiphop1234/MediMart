const orderService = require('../services/orderService');
const { serializeProduct } = require('../utils/serializers');

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const ALLOWED_STATUSES = new Set(['PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'CANCELLED']);

function serializeOrder(order) {
    const orderItems = order.order_items || [];
    return {
        _id: order.id,
        totalAmount: Number(order.total_amount),
        status: order.status,
        paymentMethod: order.payment_method,
        createdAt: order.created_at,
        updatedAt: order.updated_at,
        itemCount: orderItems.reduce((total, item) => total + item.quantity, 0),
        previewItems: orderItems.slice(0, 2).map((item) => item.product_name),
        hasMoreItems: orderItems.length > 2
    };
}

function serializeOrderDetail(order) {
    return {
        ...serializeOrder(order),
        address: {
            name: order.addresses.recipient_name,
            phone: order.addresses.phone,
            address: order.addresses.address_line
        },
        items: order.order_items.map((item) => ({
            _id: item.id,
            productId: item.product_id,
            productName: item.product_name,
            quantity: item.quantity,
            unitPrice: Number(item.unit_price),
            imageUrl: serializeProduct(item.products).imageUrl
        }))
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
            status: data.status
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMyOrders = async (req, res) => {
    try {
        const status = typeof req.query.status === 'string'
            ? req.query.status.trim().toUpperCase()
            : '';
        if (status && !ALLOWED_STATUSES.has(status)) {
            return res.status(400).json({ error: 'Invalid order status.' });
        }
        const orders = await orderService.getMyOrders(req.user.userId, status);
        res.json(orders.map(serializeOrder));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMyOrderById = async (req, res) => {
    try {
        if (!UUID_PATTERN.test(req.params.id)) {
            return res.status(400).json({ error: 'Invalid order ID.' });
        }

        const order = await orderService.getMyOrderById(req.user.userId, req.params.id);
        if (!order) {
            return res.status(404).json({ error: 'Order not found.' });
        }

        res.json(serializeOrderDetail(order));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.cancelMyOrder = async (req, res) => {
    try {
        if (!UUID_PATTERN.test(req.params.id)) {
            return res.status(400).json({ error: 'Invalid order ID.' });
        }

        const result = await orderService.cancelMyOrder(req.user.userId, req.params.id);
        if (result.outcome === 'NOT_FOUND') {
            return res.status(404).json({ error: 'Order not found.' });
        }
        if (result.outcome === 'NOT_CANCELLABLE') {
            return res.status(409).json({
                error: 'Only pending orders can be cancelled.',
                status: result.currentStatus || null
            });
        }

        res.json(serializeOrderDetail(result.order));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// --- Admin APIs ---

exports.getAllOrders = async (req, res) => {
    try {
        const page = req.query.page ? parseInt(req.query.page, 10) : 1;
        const limit = req.query.limit ? parseInt(req.query.limit, 10) : 20;
        const orders = await orderService.getAllOrders(page, limit);
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
