require('dotenv').config();

const prisma = require('../src/config/prisma');
const { getSupabaseAdmin } = require('../src/config/supabase');

const PRODUCT_IMAGE_PATH = 'seed/medimart-product.svg';
const BANNER_IMAGE_PATH = 'seed/medimart-catalog-banner.svg';

const productImage = `
<svg xmlns="http://www.w3.org/2000/svg" width="800" height="800" viewBox="0 0 800 800">
  <rect width="800" height="800" fill="#e8f5e9"/>
  <circle cx="400" cy="400" r="220" fill="#1b8f5a" opacity=".14"/>
  <rect x="245" y="220" width="310" height="390" rx="38" fill="#ffffff"/>
  <rect x="275" y="260" width="250" height="80" rx="20" fill="#1b8f5a"/>
  <text x="400" y="312" text-anchor="middle" font-family="Arial, sans-serif" font-size="42" font-weight="700" fill="#ffffff">MediMart</text>
  <text x="400" y="425" text-anchor="middle" font-family="Arial, sans-serif" font-size="38" font-weight="700" fill="#134e35">CHĂM SÓC</text>
  <text x="400" y="475" text-anchor="middle" font-family="Arial, sans-serif" font-size="38" font-weight="700" fill="#134e35">SỨC KHỎE</text>
  <circle cx="400" cy="540" r="18" fill="#f3b544"/>
</svg>`;

const bannerImage = `
<svg xmlns="http://www.w3.org/2000/svg" width="1600" height="650" viewBox="0 0 1600 650">
  <defs><linearGradient id="g" x1="0" x2="1"><stop stop-color="#0f8b5b"/><stop offset="1" stop-color="#35b879"/></linearGradient></defs>
  <rect width="1600" height="650" fill="url(#g)"/>
  <circle cx="1310" cy="110" r="270" fill="#ffffff" opacity=".12"/><circle cx="1450" cy="560" r="300" fill="#ffffff" opacity=".08"/>
  <text x="130" y="250" font-family="Arial, sans-serif" font-size="68" font-weight="700" fill="#ffffff">MediMart</text>
  <text x="130" y="340" font-family="Arial, sans-serif" font-size="46" fill="#eafff4">Chăm sóc sức khỏe mỗi ngày</text>
  <rect x="130" y="405" width="250" height="72" rx="36" fill="#ffffff"/><text x="255" y="452" text-anchor="middle" font-family="Arial, sans-serif" font-size="29" font-weight="700" fill="#0f8b5b">Khám phá ngay</text>
</svg>`;

const categories = [
    { name: 'Vitamin & khoáng chất', icon: 'https://img.icons8.com/fluency/96/pill.png' },
    { name: 'Tiêu hóa', icon: 'https://img.icons8.com/fluency/96/stomach.png' },
    { name: 'Chăm sóc da', icon: 'https://img.icons8.com/fluency/96/face-cream.png' },
    { name: 'Chăm sóc mẹ & bé', icon: 'https://img.icons8.com/fluency/96/baby-bottle.png' },
    { name: 'Thiết bị y tế', icon: 'https://img.icons8.com/fluency/96/medical-doctor.png' }
];

const products = [
    {
        category: 'Vitamin & khoáng chất',
        name: 'VitaCare Vitamin C 1000 mg',
        description: 'Viên bổ sung vitamin C, phù hợp cho nhu cầu dinh dưỡng hằng ngày.',
        price: 145000,
        sale_price: 129000,
        unit: 'Hộp 30 viên',
        brand: 'VitaCare',
        country: 'Việt Nam',
        is_flash_sale: true,
        is_best_seller: true,
        attributes: { dietarySupplement: true, form: 'Viên nén' }
    },
    {
        category: 'Vitamin & khoáng chất',
        name: 'OmegaCare 3-6-9',
        description: 'Dầu cá và acid béo thiết yếu dùng bổ sung theo nhu cầu dinh dưỡng.',
        price: 220000,
        sale_price: 199000,
        unit: 'Hộp 60 viên',
        brand: 'OmegaCare',
        country: 'Úc',
        is_flash_sale: true,
        is_best_seller: false,
        attributes: { dietarySupplement: true, form: 'Viên nang mềm' }
    },
    {
        category: 'Vitamin & khoáng chất',
        name: 'Calci D3 Daily',
        description: 'Sản phẩm bổ sung calci và vitamin D3 cho khẩu phần dinh dưỡng.',
        price: 175000,
        sale_price: null,
        unit: 'Hộp 30 viên',
        brand: 'Calci Daily',
        country: 'Việt Nam',
        is_flash_sale: false,
        is_best_seller: true,
        attributes: { dietarySupplement: true, form: 'Viên nén' }
    },
    {
        category: 'Tiêu hóa',
        name: 'DigestPro Men vi sinh',
        description: 'Men vi sinh bổ sung lợi khuẩn cho chế độ chăm sóc tiêu hóa hằng ngày.',
        price: 168000,
        sale_price: 149000,
        unit: 'Hộp 20 ống',
        brand: 'DigestPro',
        country: 'Việt Nam',
        is_flash_sale: true,
        is_best_seller: true,
        attributes: { dietarySupplement: true, form: 'Dung dịch uống' }
    },
    {
        category: 'Tiêu hóa',
        name: 'FiberBalance Chất xơ hòa tan',
        description: 'Gói chất xơ hòa tan tiện dùng để bổ sung vào chế độ ăn.',
        price: 135000,
        sale_price: null,
        unit: 'Hộp 14 gói',
        brand: 'FiberBalance',
        country: 'Việt Nam',
        is_flash_sale: false,
        is_best_seller: false,
        attributes: { dietarySupplement: true, form: 'Bột hòa tan' }
    },
    {
        category: 'Chăm sóc da',
        name: 'GentleClean Sữa rửa mặt dịu nhẹ',
        description: 'Sữa rửa mặt làm sạch dịu nhẹ, phù hợp cho quy trình chăm sóc da cơ bản.',
        price: 189000,
        sale_price: 169000,
        unit: 'Chai 200 ml',
        brand: 'GentleClean',
        country: 'Pháp',
        is_flash_sale: true,
        is_best_seller: false,
        attributes: { skinType: 'Mọi loại da', form: 'Gel' }
    },
    {
        category: 'Chăm sóc da',
        name: 'DermaShield Kem dưỡng ẩm',
        description: 'Kem dưỡng ẩm dùng cho da khô và nhạy cảm.',
        price: 245000,
        sale_price: null,
        unit: 'Tuýp 50 ml',
        brand: 'DermaShield',
        country: 'Hàn Quốc',
        is_flash_sale: false,
        is_best_seller: true,
        attributes: { skinType: 'Da khô', form: 'Kem' }
    },
    {
        category: 'Chăm sóc mẹ & bé',
        name: 'BabySoft Sữa tắm gội',
        description: 'Sữa tắm gội dịu nhẹ dành cho trẻ nhỏ.',
        price: 118000,
        sale_price: 99000,
        unit: 'Chai 250 ml',
        brand: 'BabySoft',
        country: 'Việt Nam',
        is_flash_sale: true,
        is_best_seller: true,
        attributes: { ageGroup: 'Trẻ nhỏ', form: 'Dung dịch' }
    },
    {
        category: 'Chăm sóc mẹ & bé',
        name: 'MamaCare Miếng lót thấm sữa',
        description: 'Miếng lót thấm sữa dùng một lần, tiện lợi cho mẹ sau sinh.',
        price: 92000,
        sale_price: null,
        unit: 'Hộp 30 miếng',
        brand: 'MamaCare',
        country: 'Việt Nam',
        is_flash_sale: false,
        is_best_seller: false,
        attributes: { ageGroup: 'Mẹ sau sinh', form: 'Miếng lót' }
    },
    {
        category: 'Thiết bị y tế',
        name: 'ThermoCheck Nhiệt kế điện tử',
        description: 'Nhiệt kế điện tử cho nhu cầu theo dõi thân nhiệt tại nhà.',
        price: 175000,
        sale_price: 149000,
        unit: 'Cái',
        brand: 'ThermoCheck',
        country: 'Nhật Bản',
        is_flash_sale: true,
        is_best_seller: true,
        attributes: { warranty: '12 tháng', type: 'Điện tử' }
    },
    {
        category: 'Thiết bị y tế',
        name: 'MediMask Khẩu trang y tế 4 lớp',
        description: 'Khẩu trang y tế dùng một lần cho nhu cầu sử dụng hằng ngày.',
        price: 45000,
        sale_price: null,
        unit: 'Hộp 50 cái',
        brand: 'MediMask',
        country: 'Việt Nam',
        is_flash_sale: false,
        is_best_seller: true,
        attributes: { layers: 4, type: 'Dùng một lần' }
    }
];

const banners = [
    { image_path: BANNER_IMAGE_PATH, link_to: '/products/flash-sale', position: 1 },
    { image_path: BANNER_IMAGE_PATH, link_to: '/products/best-seller', position: 2 },
    { image_path: BANNER_IMAGE_PATH, link_to: '/rewards', position: 3 }
];

async function ensureStorageAsset(bucket, path, content) {
    const { error } = await getSupabaseAdmin().storage
        .from(bucket)
        .upload(path, Buffer.from(content), {
            contentType: 'image/svg+xml',
            cacheControl: '3600',
            upsert: true
        });
    if (error) throw error;
}

async function seed() {
    let storageAvailable = true;
    try {
        await ensureStorageAsset('product-images', PRODUCT_IMAGE_PATH, productImage);
        await ensureStorageAsset('banner-images', BANNER_IMAGE_PATH, bannerImage);
    } catch (error) {
        storageAvailable = false;
        console.warn(`Storage assets were skipped: ${error.name}`);
    }
    const categoryByName = new Map();

    for (const category of categories) {
        const existing = await prisma.categories.findFirst({ where: { name: category.name } });
        const record = existing || await prisma.categories.create({ data: category });
        categoryByName.set(category.name, record);
    }

    const saleEnd = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
    for (const product of products) {
        const { category, ...data } = product;
        const existing = await prisma.products.findFirst({ where: { name: data.name } });
        const payload = {
            ...data,
            category_id: categoryByName.get(category).id,
            image_path: storageAvailable ? PRODUCT_IMAGE_PATH : null,
            flash_sale_ends_at: data.is_flash_sale ? saleEnd : null
        };
        if (!existing) {
            await prisma.products.create({
                data: payload
            });
        } else if (!existing.image_path) {
            await prisma.products.update({ where: { id: existing.id }, data: payload });
        }
    }

    if (storageAvailable) {
        for (const banner of banners) {
            const existing = await prisma.banners.findFirst({ where: { link_to: banner.link_to } });
            if (!existing) await prisma.banners.create({ data: banner });
        }
    }

    for (const category of categoryByName.values()) {
        const productCount = await prisma.products.count({ where: { category_id: category.id } });
        await prisma.categories.update({ where: { id: category.id }, data: { product_count: productCount } });
    }

    const [categoryCount, productCount, bannerCount] = await Promise.all([
        prisma.categories.count(),
        prisma.products.count(),
        prisma.banners.count()
    ]);
    console.log(JSON.stringify({ categories: categoryCount, products: productCount, banners: bannerCount, storageAvailable }));
}

seed()
    .catch((error) => {
        console.error(error);
        process.exitCode = 1;
    })
    .finally(() => prisma.$disconnect());
