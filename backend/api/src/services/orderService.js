const prisma = require('../config/prisma');
const { getSupabaseUserClient } = require('../config/supabase');

class OrderService {
    async checkout(items, addressId, paymentMethod, accessToken) {
        const normalizedItems = items.map((item) => ({
            product_id: item.productId,
            quantity: Number(item.quantity)
        }));
        
        // We still use Supabase RPC for checkout to leverage the existing PL/pgSQL transaction
        const { data, error } = await getSupabaseUserClient(accessToken).rpc('checkout', {
            p_items: normalizedItems,
            p_address_id: addressId,
            p_payment_method: paymentMethod
        });

        if (error) throw new Error(error.message);
        return data;
    }

    async getMyOrders(userId, status) {
        const allowedStatuses = new Set(['PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED', 'CANCELLED']);
        const whereClause = { user_id: userId };
        
        if (status && allowedStatuses.has(status)) {
            whereClause.status = status;
        }

        return await prisma.orders.findMany({
            where: whereClause,
            orderBy: { created_at: 'desc' }
        });
    }

    // --- Admin APIs ---

    async getAllOrders() {
        return await prisma.orders.findMany({
            orderBy: { created_at: 'desc' },
            include: {
                profiles: true,
                addresses: true,
                order_items: {
                    include: { products: true }
                }
            }
        });
    }

    async getOrderById(id) {
        return await prisma.orders.findUnique({
            where: { id },
            include: {
                profiles: true,
                addresses: true,
                order_items: {
                    include: { products: true }
                }
            }
        });
    }

    async updateOrderStatus(id, status) {
        return await prisma.orders.update({
            where: { id },
            data: { status }
        });
    }
}

module.exports = new OrderService();
