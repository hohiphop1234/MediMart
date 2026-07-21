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
                    status: true,
                    earned_points: true
                }
            });

            if (!order) {
                return { outcome: 'NOT_FOUND' };
            }
            if (order.status !== 'PENDING') {
                return { outcome: 'NOT_CANCELLABLE', currentStatus: order.status };
            }

            if (order.earned_points > 0) {
                const profile = await transaction.profiles.findUnique({
                    where: { id: userId },
                    select: { loyalty_points: true }
                });
                if (!profile || profile.loyalty_points < order.earned_points) {
                    return { outcome: 'POINTS_ALREADY_USED' };
                }
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

            if (order.earned_points > 0) {
                const pointsReversed = await transaction.profiles.updateMany({
                    where: {
                        id: userId,
                        loyalty_points: { gte: order.earned_points }
                    },
                    data: {
                        loyalty_points: { decrement: order.earned_points }
                    }
                });

                if (pointsReversed.count !== 1) {
                    throw new Error('Unable to reverse order points safely.');
                }

                await transaction.point_transactions.create({
                    data: {
                        user_id: userId,
                        delta: -order.earned_points,
                        reason: 'ADMIN_ADJUSTMENT',
                        order_id: id
                    }
                });
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
        }, {
            isolationLevel: 'Serializable'
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
