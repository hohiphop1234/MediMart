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

    async searchProducts(q, categoryId = '', sortBy = 'relevance', page = 1, limit = 20) {
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

        let orderBy;
        if (sortBy === 'price_asc') {
            orderBy = [{ price: 'asc' }, { created_at: 'desc' }];
        } else if (sortBy === 'price_desc') {
            orderBy = [{ price: 'desc' }, { created_at: 'desc' }];
        } else {
            orderBy = [
                { is_best_seller: 'desc' },
                { created_at: 'desc' }
            ];
        }

        const pageNum = Math.max(1, parseInt(page, 10) || 1);
        const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 20));
        const skip = (pageNum - 1) * limitNum;

        return await prisma.products.findMany({
            where,
            orderBy,
            skip,
            take: limitNum
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
        const name = typeof data.name === 'string' ? data.name.trim() : '';
        if (!name || name.length > 255) {
            throw new Error('Product name is required and must not exceed 255 characters.');
        }

        const payload = {
            name,
            description: data.description || null,
            price: data.price !== undefined ? data.price : 0,
            sale_price: data.salePrice !== undefined && data.salePrice !== '' ? data.salePrice : (data.sale_price || null),
            unit: data.unit || null,
            image_path: data.imagePath || data.image_path || data.imageUrl || null,
            category_id: data.categoryId || data.category_id || null,
            brand: data.brand || null,
            country: data.country || null,
            is_flash_sale: data.isFlashSale !== undefined ? Boolean(data.isFlashSale) : Boolean(data.is_flash_sale),
            is_best_seller: data.isBestSeller !== undefined ? Boolean(data.isBestSeller) : Boolean(data.is_best_seller)
        };
        return await prisma.products.create({
            data: payload,
            include: { categories: true }
        });
    }

    async updateProduct(id, data) {
        const payload = {};
        if (data.name !== undefined) {
            const name = typeof data.name === 'string' ? data.name.trim() : '';
            if (!name || name.length > 255) {
                throw new Error('Product name must not be empty or exceed 255 characters.');
            }
            payload.name = name;
        }
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
