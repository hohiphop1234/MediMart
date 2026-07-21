require('dotenv').config();

const prisma = require('../src/config/prisma');

const source = 'https://nhathuoclongchau.com.vn/';
const capturedAt = '2026-07-21T00:00:00+07:00';

const catalog = [
    {
        category: 'Thuốc',
        name: 'Hỗn dịch uống men vi sinh Enterogermina Gut Defense 2 tỷ/5ml Opella (2 vỉ 10 ống)',
        description: 'Hỗn dịch uống men vi sinh Enterogermina Gut Defense 2 tỷ/5ml Opella.',
        price: 184000,
        sale_price: null,
        unit: 'Hộp 2 vỉ x 10 ống x 5ml',
        brand: 'Opella',
        country: 'Pháp',
        is_best_seller: true,
        source_url: 'https://nhathuoclongchau.com.vn/thuoc/enterogermina-gut-defense-2-ty-opella-2-x10-ong.html'
    },
    {
        category: 'Thực phẩm chức năng',
        name: 'Viên uống NMN Premium 21600 Jpanwell (60 viên)',
        description: 'Viên uống NMN Premium 21600 Jpanwell.',
        price: 8900000,
        sale_price: 5785000,
        unit: 'Hộp 60 viên',
        brand: 'Jpanwell',
        country: 'Nhật Bản',
        is_flash_sale: true,
        is_best_seller: true,
        source_url: 'https://nhathuoclongchau.com.vn/thuc-pham-chuc-nang/vien-uong-truong-tho-tre-hoa-da-nmn-premium-21600-60v-37581.html'
    },
    {
        category: 'Thực phẩm chức năng',
        name: 'Viên uống Gasso Max Vitamins For Life (30 viên)',
        description: 'Viên uống bổ sung enzyme và thảo mộc Gasso Max Vitamins For Life.',
        price: 335000,
        sale_price: 251250,
        unit: 'Chai 30 viên',
        brand: 'Vitamins For Life',
        country: 'Hoa Kỳ',
        is_flash_sale: true,
        source_url: 'https://nhathuoclongchau.com.vn/thuc-pham-chuc-nang/gasso-max-thao-duoc-ho-tro-tieu-hoa-45.html'
    },
    {
        category: 'Thực phẩm chức năng',
        name: 'Viên uống Nano Fucoidan Biochempha (30 viên)',
        description: 'Viên uống Nano Fucoidan Biochempha 30 viên.',
        price: 990000,
        sale_price: 792000,
        unit: 'Hộp 30 viên',
        brand: 'Biochempha',
        country: 'Việt Nam',
        is_flash_sale: true,
        source_url: 'https://nhathuoclongchau.com.vn/thuc-pham-chuc-nang/vien-uong-ho-tro-chong-oxy-hoa-han-che-goc-tu-do-tang-cuong-suc-khoe-nano-fucoidan-biochempha-30-v.html'
    },
    {
        category: 'Thực phẩm chức năng',
        name: 'Brauer Baby & Kids Ultra Pure DHA (60 viên)',
        description: 'Viên Brauer Baby & Kids Ultra Pure DHA.',
        price: 486000,
        sale_price: 388800,
        unit: 'Hộp 60 viên',
        brand: 'Brauer',
        country: 'Úc',
        is_flash_sale: true,
        source_url: 'https://nhathuoclongchau.com.vn/thuc-pham-chuc-nang/brauer-baby-kids-ultra-pure-dha-60v-33779.html'
    },
    {
        category: 'Chăm sóc cá nhân',
        name: 'Nước Sâm Nguyên Củ Achimmadang Inbosam Biok Korea Root Drink (10 chai x 120ml)',
        description: 'Nước sâm nguyên củ Achimmadang Inbosam Biok Korea Root Drink.',
        price: 500000,
        sale_price: 400000,
        unit: 'Hộp 10 chai x 120ml',
        brand: 'Biok Korea',
        country: 'Hàn Quốc',
        is_flash_sale: true,
        source_url: 'https://nhathuoclongchau.com.vn/cham-soc-ca-nhan/nuoc-sam-nguyen-cu-achimmadang-inbosam-biok-korea-root-dkink-10-chai-x-120ml-37190.html'
    }
];

async function findOrCreateCategory(name) {
    const existing = await prisma.categories.findFirst({ where: { name } });
    if (existing) return existing;
    return prisma.categories.create({ data: { name } });
}

async function importCatalog() {
    const categoryByName = new Map();
    for (const item of catalog) {
        if (!categoryByName.has(item.category)) {
            categoryByName.set(item.category, await findOrCreateCategory(item.category));
        }
    }

    const saleEnd = new Date(Date.now() + 24 * 60 * 60 * 1000);
    let created = 0;
    let skipped = 0;
    for (const item of catalog) {
        const existing = await prisma.products.findFirst({ where: { name: item.name } });
        if (existing) {
            skipped += 1;
            continue;
        }
        const { category, source_url, ...product } = item;
        await prisma.products.create({
            data: {
                ...product,
                category_id: categoryByName.get(category).id,
                is_best_seller: product.is_best_seller || false,
                is_flash_sale: product.is_flash_sale || false,
                flash_sale_ends_at: product.is_flash_sale ? saleEnd : null,
                attributes: {
                    source,
                    sourceUrl: source_url,
                    capturedAt,
                    imageStatus: 'not-imported'
                }
            }
        });
        created += 1;
    }

    for (const category of categoryByName.values()) {
        const productCount = await prisma.products.count({ where: { category_id: category.id } });
        await prisma.categories.update({ where: { id: category.id }, data: { product_count: productCount } });
    }

    console.log(JSON.stringify({ source, capturedAt, created, skipped }));
}

importCatalog()
    .catch((error) => {
        console.error(error);
        process.exitCode = 1;
    })
    .finally(() => prisma.$disconnect());
