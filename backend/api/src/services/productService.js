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
