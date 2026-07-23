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
            orderBy: { created_at: 'desc' },
            include: {
                order_items: {
                    select: {
                        product_name: true,
                        quantity: true
                    }
                }
            }
        });
    }

    async getMyOrderById(userId, id) {
        return await prisma.orders.findFirst({
            where: {
                id,
                user_id: userId
            },
            include: {
                addresses: true,
                order_items: {
                    include: {
                        products: true
                    }
                }
            }
        });
    }

    async cancelMyOrder(userId, id) {
        return await prisma.$transaction(async (transaction) => {
            const order = await transaction.orders.findFirst({
                where: {
                    id,
                    user_id: userId
                },
                select: {
                    status: true
                }
            });

            if (!order) {
                return { outcome: 'NOT_FOUND' };
            }
            if (order.status !== 'PENDING') {
                return { outcome: 'NOT_CANCELLABLE', currentStatus: order.status };
            }

            const cancelled = await transaction.orders.updateMany({
                where: {
                    id,
                    user_id: userId,
                    status: 'PENDING'
                },
                data: { status: 'CANCELLED' }
            });

            if (cancelled.count !== 1) {
                return { outcome: 'NOT_CANCELLABLE' };
            }

            const updatedOrder = await transaction.orders.findFirst({
                where: {
                    id,
                    user_id: userId
                },
                include: {
                    addresses: true,
                    order_items: {
                        include: {
                            products: true
                        }
                    }
                }
            });

            return { outcome: 'CANCELLED', order: updatedOrder };
        });
    }

    // --- Admin APIs ---

    async getAllOrders(page = 1, limit = 20) {
        const pageNum = Math.max(1, parseInt(page, 10) || 1);
        const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));
        const skip = (pageNum - 1) * limitNum;

        return await prisma.orders.findMany({
            orderBy: { created_at: 'desc' },
            skip,
            take: limitNum,
            include: {
                users: {
                    include: {
                        profiles: true
                    }
                },
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
                users: {
                    include: {
                        profiles: true
                    }
                },
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
