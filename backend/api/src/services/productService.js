const prisma = require('../config/prisma');

class ProductService {
    async getFlashSale() {
        const products = await prisma.products.findMany({
            where: { is_flash_sale: true },
            orderBy: { created_at: 'desc' }
        });
        return products;
    }

    async getBestSellers() {
        const products = await prisma.products.findMany({
            where: { is_best_seller: true },
            orderBy: { created_at: 'desc' }
        });
        return products;
    }

    async searchProducts(q) {
        if (!q) {
            return await prisma.products.findMany({
                orderBy: { created_at: 'desc' }
            });
        }
        return await prisma.products.findMany({
            where: {
                name: {
                    contains: q,
                    mode: 'insensitive' // Requires postgresql
                }
            },
            orderBy: { created_at: 'desc' }
        });
    }

    // Admin CRUD
    async getAllProducts() {
        return await prisma.products.findMany({
            orderBy: { created_at: 'desc' },
            include: { categories: true }
        });
    }

    async getProductById(id) {
        return await prisma.products.findUnique({
            where: { id }
        });
    }

    async createProduct(data) {
        const payload = {
            name: data.name,
            description: data.description || null,
            price: data.price !== undefined ? data.price : 0,
            sale_price: data.salePrice !== undefined && data.salePrice !== '' ? data.salePrice : (data.sale_price || null),
            unit: data.unit || null,
            image_path: data.imagePath || data.image_path || data.imageUrl || null,
            category_id: data.categoryId || data.category_id || null,
            brand: data.brand || null,
            country: data.country || null,
            is_flash_sale: data.isFlashSale !== undefined ? Boolean(data.isFlashSale) : Boolean(data.is_flash_sale),
            is_best_seller: data.isBestSeller !== undefined ? Boolean(data.isBestSeller) : Boolean(data.is_best_seller),
            is_reward_item: data.isRewardItem !== undefined ? Boolean(data.isRewardItem) : Boolean(data.is_reward_item),
            point_price: data.pointPrice !== undefined && data.pointPrice !== '' ? Number(data.pointPrice) : (data.point_price || null)
        };
        return await prisma.products.create({
            data: payload,
            include: { categories: true }
        });
    }

    async updateProduct(id, data) {
        const payload = {};
        if (data.name !== undefined) payload.name = data.name;
        if (data.description !== undefined) payload.description = data.description;
        if (data.price !== undefined) payload.price = data.price;
        if (data.salePrice !== undefined || data.sale_price !== undefined) {
            payload.sale_price = data.salePrice !== undefined ? (data.salePrice || null) : data.sale_price;
        }
        if (data.unit !== undefined) payload.unit = data.unit;
        if (data.imagePath !== undefined || data.image_path !== undefined || data.imageUrl !== undefined) {
            payload.image_path = data.imagePath || data.image_path || data.imageUrl || null;
        }
        if (data.categoryId !== undefined || data.category_id !== undefined) {
            payload.category_id = data.categoryId || data.category_id || null;
        }
        if (data.brand !== undefined) payload.brand = data.brand;
        if (data.country !== undefined) payload.country = data.country;
        if (data.isFlashSale !== undefined || data.is_flash_sale !== undefined) {
            payload.is_flash_sale = data.isFlashSale !== undefined ? Boolean(data.isFlashSale) : Boolean(data.is_flash_sale);
        }
        if (data.isBestSeller !== undefined || data.is_best_seller !== undefined) {
            payload.is_best_seller = data.isBestSeller !== undefined ? Boolean(data.isBestSeller) : Boolean(data.is_best_seller);
        }

        return await prisma.products.update({
            where: { id },
            data: payload,
            include: { categories: true }
        });
    }

    async deleteProduct(id) {
        return await prisma.products.delete({
            where: { id }
        });
    }

    // Similar Products for Python OCR
    async getSimilarProducts(medicineName) {
        if (!medicineName) return [];
        // Basic implementation: search by name contains
        // Can be improved later using Postgres Full Text Search or Embeddings
        return await prisma.products.findMany({
            where: {
                OR: [
                    { name: { contains: medicineName, mode: 'insensitive' } },
                    { description: { contains: medicineName, mode: 'insensitive' } }
                ]
            },
            take: 5
        });
    }
}

module.exports = new ProductService();
