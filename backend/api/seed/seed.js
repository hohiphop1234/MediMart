const mongoose = require('mongoose');
const dotenv = require('dotenv');
// Import models
const User = require('../models/User');
const Product = require('../models/Product');
const Category = require('../models/Category');
const Banner = require('../models/Banner');
const Order = require('../models/Order');
const Address = require('../models/Address');

// Load .env relative to the seed folder (which is one level deep)
dotenv.config({ path: require('path').resolve(__dirname, '../.env') });

const seedData = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('Connected to DB...');

        // Clear existing data
        await User.deleteMany();
        await Product.deleteMany();
        await Category.deleteMany();
        await Banner.deleteMany();
        await Order.deleteMany();
        await Address.deleteMany();

        console.log('Cleared existing data. Inserting new data...');

        // ==========================================
        // 1. ADD BANNERS HERE
        // ==========================================
        const banners = await Banner.insertMany([
            // TODO: Thay thế imageUrl bằng link ảnh thật của Long Châu (ví dụ từ Firebase Storage hoặc copy image address)
            { imageUrl: 'https://placehold.co/800x400/1A56DB/FFF?text=Banner+1', linkTo: '/sale', order: 1 },
            { imageUrl: 'https://placehold.co/800x400/FF0000/FFF?text=Banner+2', linkTo: '/news', order: 2 }
        ]);

        // ==========================================
        // 2. ADD CATEGORIES HERE
        // ==========================================
        const categoriesData = [
            // TODO: Thay icon bằng link ảnh icon thật của các danh mục
            { name: 'Thần kinh não', icon: 'https://placehold.co/100x100?text=Icon', productCount: 44 },
            { name: 'Vitamin & Khoáng chất', icon: 'https://placehold.co/100x100?text=Icon', productCount: 79 },
            { name: 'Tim mạch - Huyết áp', icon: 'https://placehold.co/100x100?text=Icon', productCount: 17 },
            { name: 'Miễn dịch - Đề kháng', icon: 'https://placehold.co/100x100?text=Icon', productCount: 38 },
            { name: 'Tiêu hóa', icon: 'https://placehold.co/100x100?text=Icon', productCount: 50 }
        ];
        const categories = await Category.insertMany(categoriesData);

        // ==========================================
        // 3. ADD PRODUCTS HERE
        // ==========================================
        // Chú ý: Cần lấy categoryId từ mảng categories đã insert ở trên để gán vào sản phẩm
        const productsData = [
            {
                name: 'Men vi sinh Enterogermina 2B C/20 (Sanofi)', // TODO: Tên thật
                description: 'Men vi sinh hỗ trợ tiêu hóa, giảm rối loạn khuẩn đường ruột.', // TODO: Mô tả thật
                price: 156000, // TODO: Giá thật
                salePrice: 150000, // Nếu đang giảm giá
                unit: 'Hộp',
                imageUrl: 'https://placehold.co/300x300?text=Enterogermina', // TODO: Copy url ảnh thật từ web Long Châu dán vào đây
                categoryId: categories[4]._id, // Danh mục Tiêu hóa
                brand: 'Sanofi',
                country: 'Pháp',
                isFlashSale: true, // Hiển thị trên màn FlashSale
                isBestSeller: true, // Hiển thị trên mục Bán Chạy
                isRewardItem: false,
                pointPrice: 0
            },
            {
                name: 'Viên uống NMN Premium 3300', 
                description: 'Hỗ trợ chống lão hóa, phục hồi sức khỏe nhanh chóng...', 
                price: 1200000, 
                salePrice: null,
                unit: 'Lọ',
                imageUrl: 'https://placehold.co/300x300?text=NMN', 
                categoryId: categories[1]._id, // Danh mục Vitamin & Khoáng chất
                brand: 'Nhật Bản',
                country: 'Nhật Bản',
                isFlashSale: false,
                isBestSeller: true,
                isRewardItem: true, // Sản phẩm này cho phép đổi điểm!
                pointPrice: 5000 // Số điểm cần để đổi
            }
            // TODO: BẠN HÃY COPY PAST VÀ THÊM KHOẢNG 10-20 SẢN PHẨM NỮA Ở ĐÂY ĐỂ APP NHÌN CHÂN THỰC NHÉ!
        ];
        await Product.insertMany(productsData);

        // ==========================================
        // 4. ADD TEST USER (Optional)
        // ==========================================
        const testUser = new User({
            name: 'Võ Trúc Hồ',
            phone: '0916059451',
            loyaltyPoints: 709 // Cấp sẵn điểm để bạn test tính năng đổi quà ở màn hình Rewards
        });
        await testUser.save();

        console.log('✅ Data Imported Successfully!');
        process.exit();
    } catch (err) {
        console.error('❌ Seed Error:', err);
        process.exit(1);
    }
};

seedData();
