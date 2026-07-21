const path = require('path');
const dotenv = require('dotenv');
dotenv.config({ path: path.resolve(__dirname, '../.env') });

const prisma = require('../src/config/prisma');

const seedData = async () => {
    try {
        console.log('Clearing existing data...');
        await prisma.point_transactions.deleteMany();
        await prisma.reward_redemptions.deleteMany();
        await prisma.order_items.deleteMany();
        await prisma.orders.deleteMany();
        await prisma.products.deleteMany();
        await prisma.categories.deleteMany();
        await prisma.banners.deleteMany();

        console.log('Inserting banners...');
        await prisma.banners.createMany({
            data: [
                { image_path: 'https://placehold.co/800x400/1A56DB/FFF?text=Banner+1', link_to: '/sale', position: 1 },
                { image_path: 'https://placehold.co/800x400/FF0000/FFF?text=Banner+2', link_to: '/news', position: 2 }
            ]
        });

        console.log('Inserting categories...');
        const categoriesData = [
            { name: 'Thần kinh não', icon: 'https://placehold.co/100x100?text=Brain', product_count: 1 },
            { name: 'Vitamin & Khoáng chất', icon: 'https://placehold.co/100x100?text=Vitamins', product_count: 1 },
            { name: 'Tim mạch - Huyết áp', icon: 'https://placehold.co/100x100?text=Heart', product_count: 0 },
            { name: 'Miễn dịch - Đề kháng', icon: 'https://placehold.co/100x100?text=Immune', product_count: 0 },
            { name: 'Tiêu hóa', icon: 'https://placehold.co/100x100?text=Digestive', product_count: 1 }
        ];

        const createdCategories = [];
        for (const cat of categoriesData) {
            const created = await prisma.categories.create({ data: cat });
            createdCategories.push(created);
        }

        console.log('Inserting products...');
        await prisma.products.createMany({
            data: [
                {
                    name: 'Men vi sinh Enterogermina 2B C/20 (Sanofi)',
                    description: 'Men vi sinh hỗ trợ tiêu hóa, giảm rối loạn khuẩn đường ruột.',
                    price: 156000,
                    sale_price: 150000,
                    unit: 'Hộp',
                    image_path: 'https://placehold.co/300x300?text=Enterogermina',
                    category_id: createdCategories[4].id, // Tiêu hóa
                    brand: 'Sanofi',
                    country: 'Pháp',
                    is_flash_sale: true,
                    is_best_seller: true,
                    is_reward_item: false,
                    point_price: null
                },
                {
                    name: 'Viên uống NMN Premium 3300',
                    description: 'Hỗ trợ chống lão hóa, phục hồi sức khỏe nhanh chóng...',
                    price: 1200000,
                    sale_price: null,
                    unit: 'Lọ',
                    image_path: 'https://placehold.co/300x300?text=NMN',
                    category_id: createdCategories[1].id, // Vitamin & Khoáng chất
                    brand: 'Nhật Bản',
                    country: 'Nhật Bản',
                    is_flash_sale: false,
                    is_best_seller: true,
                    is_reward_item: true,
                    point_price: 5000
                },
                {
                    name: 'Hoạt huyết dưỡng não Cerebon Extra',
                    description: 'Tăng cường tuần hoàn não, giảm đau đầu chóng mặt...',
                    price: 250000,
                    sale_price: 220000,
                    unit: 'Hộp',
                    image_path: 'https://placehold.co/300x300?text=Cerebon',
                    category_id: createdCategories[0].id, // Thần kinh extra
                    brand: 'MediMart',
                    country: 'Việt Nam',
                    is_flash_sale: true,
                    is_best_seller: false,
                    is_reward_item: false,
                    point_price: null
                }
            ]
        });

        console.log('✅ Data Imported Successfully!');
        process.exit(0);
    } catch (err) {
        console.error('❌ Seed Error:', err);
        process.exit(1);
    }
};

seedData();
