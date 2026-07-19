const { getSupabaseUserClient } = require('../config/supabase');

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

        const normalizedItems = items.map((item) => ({
            product_id: item.productId,
            quantity: Number(item.quantity)
        }));
        const { data, error } = await getSupabaseUserClient(req.accessToken).rpc('checkout', {
            p_items: normalizedItems,
            p_address_id: addressId,
            p_payment_method: paymentMethod
        });
        if (error) return res.status(400).json({ error: error.message });

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
        const allowedStatuses = new Set(['PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'CANCELLED']);
        const status = typeof req.query.status === 'string' ? req.query.status : '';
        let query = getSupabaseUserClient(req.accessToken)
            .from('orders')
            .select('*')
            .order('created_at', { ascending: false });
        if (allowedStatuses.has(status)) query = query.eq('status', status);

        const { data, error } = await query;
        if (error) throw error;
        res.json(data.map(serializeOrder));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
