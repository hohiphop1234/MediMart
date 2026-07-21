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

    async searchProducts(q, categoryId = '') {
        const where = {};

        if (categoryId) {
            where.category_id = categoryId;
        }

        if (q) {
            where.OR = [
                { name: { contains: q, mode: 'insensitive' } },
                { brand: { contains: q, mode: 'insensitive' } },
                { description: { contains: q, mode: 'insensitive' } },
                { country: { contains: q, mode: 'insensitive' } }
            ];
        }

        return await prisma.products.findMany({
            where,
            orderBy: [
                { is_best_seller: 'desc' },
                { created_at: 'desc' }
            ],
            take: 100
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
        return await prisma.products.create({
            data
        });
    }

    async updateProduct(id, data) {
        return await prisma.products.update({
            where: { id },
            data
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
