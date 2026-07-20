const prisma = require('../config/prisma');

class CategoryService {
    async getAllCategories() {
        return await prisma.categories.findMany({
            orderBy: { name: 'asc' }
        });
    }

    async getCategoryById(id) {
        return await prisma.categories.findUnique({
            where: { id }
        });
    }

    async createCategory(data) {
        return await prisma.categories.create({
            data
        });
    }

    async updateCategory(id, data) {
        return await prisma.categories.update({
            where: { id },
            data
        });
    }

    async deleteCategory(id) {
        return await prisma.categories.delete({
            where: { id }
        });
    }
}

module.exports = new CategoryService();
