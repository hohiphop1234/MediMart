require('dotenv').config();

const prisma = require('../src/config/prisma');
const { getSupabaseAdmin } = require('../src/config/supabase');

const BASE_URL = 'https://nhathuoclongchau.com.vn';
const REQUEST_DELAY_MS = 750;
const MAX_PRODUCTS_PER_CATEGORY = 20;
const dryRun = process.argv.includes('--dry-run');

const categories = [
    { name: 'Thuốc', path: '/thuoc', productPath: '/thuoc' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang', productPath: '/thuc-pham-chuc-nang' },
    { name: 'Dược mỹ phẩm', path: '/duoc-my-pham', productPath: '/duoc-my-pham' },
    { name: 'Chăm sóc cá nhân', path: '/cham-soc-ca-nhan', productPath: '/cham-soc-ca-nhan' },
    { name: 'Thiết bị y tế', path: '/trang-thiet-bi-y-te', productPath: '/trang-thiet-bi-y-te' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang/vitamin-khoang-chat', productPath: '/thuc-pham-chuc-nang' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang/ho-tro-mien-dich-tang-suc-de-khang', productPath: '/thuc-pham-chuc-nang' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang/ho-tro-tieu-hoa', productPath: '/thuc-pham-chuc-nang' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang/than-kinh-nao', productPath: '/thuc-pham-chuc-nang' },
    { name: 'Thực phẩm chức năng', path: '/thuc-pham-chuc-nang/co-xuong-khop', productPath: '/thuc-pham-chuc-nang' }
];

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function decodeHtml(value = '') {
    return value
        .replace(/&amp;/g, '&')
        .replace(/&quot;/g, '"')
        .replace(/&#x27;/g, "'")
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&#(\d+);/g, (_, code) => String.fromCharCode(Number(code)))
        .replace(/\s+/g, ' ')
        .trim();
}

function stripHtml(value = '') {
    return decodeHtml(value.replace(/<[^>]+>/g, ' '));
}

function currencyToNumber(value) {
    const digits = String(value || '').replace(/[^0-9]/g, '');
    return digits ? Number(digits) : null;
}

async function fetchText(url) {
    const response = await fetch(url, {
        headers: { 'User-Agent': 'MediMartCatalogImporter/1.0' }
    });
    if (!response.ok) throw new Error(`Request failed (${response.status}): ${url}`);
    return response.text();
}

function extractProductLinks(html, categoryPath) {
    const expression = new RegExp(`href="(${categoryPath.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}/[^"?#]+\\.html)"`, 'g');
    const links = new Set();
    for (const match of html.matchAll(expression)) {
        const path = decodeHtml(match[1]);
        if (!path.includes('/bai-viet/')) links.add(`${BASE_URL}${path}`);
    }
    return [...links].slice(0, MAX_PRODUCTS_PER_CATEGORY);
}

function extractProduct(html, sourceUrl, category) {
    const nameMatch = html.match(/<h1[^>]*>([\s\S]*?)<\/h1>/i);
    const priceMatch = html.match(/data-test="price"[^>]*>([^<]+)</i);
    const unitMatch = html.match(/data-test="unit"[^>]*>([^<]+)</i);
    const descriptionMatch = html.match(/<meta[^>]+name="description"[^>]+content="([^"]+)"/i);
    const imageMatch = html.match(/https:\/\/cdn\.nhathuoclongchau\.com\.vn\/unsafe\/640x0\/filters:quality\(90\):format\(webp\)\/[^"'\s<>]+/i);

    const name = stripHtml(nameMatch?.[1]);
    const price = currencyToNumber(priceMatch?.[1]);
    if (!name || !price || !imageMatch) return null;

    return {
        category,
        name,
        description: decodeHtml(descriptionMatch?.[1] || name),
        price,
        unit: decodeHtml(unitMatch?.[1] || ''),
        sourceUrl,
        imageSourceUrl: decodeHtml(imageMatch[0])
    };
}

function storagePathFor(sourceUrl) {
    const slug = sourceUrl.split('/').pop().replace(/\.html$/, '').replace(/[^a-z0-9]+/gi, '-').replace(/^-|-$/g, '');
    return `longchau/catalog/${slug}.webp`;
}

async function getCategory(name) {
    const existing = await prisma.categories.findFirst({ where: { name } });
    return existing || prisma.categories.create({ data: { name } });
}

async function importCatalog() {
    const discovered = [];
    for (const category of categories) {
        const html = await fetchText(`${BASE_URL}${category.path}`);
        const links = extractProductLinks(html, category.productPath);
        for (const link of links) discovered.push({ ...category, link });
        await sleep(REQUEST_DELAY_MS);
    }

    const unique = [...new Map(discovered.map((item) => [item.link, item])).values()];
    const records = [];
    for (const item of unique) {
        try {
            const html = await fetchText(item.link);
            const product = extractProduct(html, item.link, item.name);
            if (product) records.push(product);
        } catch (error) {
            console.warn(`Skipped ${item.link}: ${error.message}`);
        }
        await sleep(REQUEST_DELAY_MS);
    }

    if (dryRun) {
        console.log(JSON.stringify({ discovered: unique.length, parsed: records.length, dryRun: true }));
        return;
    }

    const categoryByName = new Map();
    for (const category of categories) categoryByName.set(category.name, await getCategory(category.name));

    const storage = getSupabaseAdmin().storage.from('product-images');
    let created = 0;
    let skipped = 0;
    let imageFailures = 0;

    for (const product of records) {
        const existing = await prisma.products.findFirst({ where: { name: product.name } });
        if (existing) {
            skipped += 1;
            continue;
        }

        const path = storagePathFor(product.sourceUrl);
        let imagePath = null;
        try {
            const imageResponse = await fetch(product.imageSourceUrl);
            if (!imageResponse.ok) throw new Error(`image ${imageResponse.status}`);
            const { error } = await storage.upload(path, Buffer.from(await imageResponse.arrayBuffer()), {
                contentType: imageResponse.headers.get('content-type') || 'image/webp',
                cacheControl: '86400',
                upsert: true
            });
            if (error) throw error;
            imagePath = path;
        } catch (error) {
            imageFailures += 1;
            console.warn(`Image skipped for ${product.name}: ${error.message}`);
        }

        await prisma.products.create({
            data: {
                category_id: categoryByName.get(product.category).id,
                name: product.name,
                description: product.description,
                price: product.price,
                unit: product.unit || null,
                image_path: imagePath,
                attributes: {
                    source: BASE_URL,
                    sourceUrl: product.sourceUrl,
                    imageSourceUrl: product.imageSourceUrl,
                    capturedAt: new Date().toISOString()
                }
            }
        });
        created += 1;
    }

    for (const category of categoryByName.values()) {
        const productCount = await prisma.products.count({ where: { category_id: category.id } });
        await prisma.categories.update({ where: { id: category.id }, data: { product_count: productCount } });
    }

    console.log(JSON.stringify({ discovered: unique.length, parsed: records.length, created, skipped, imageFailures }));
}

importCatalog()
    .catch((error) => {
        console.error(error);
        process.exitCode = 1;
    })
    .finally(async () => {
        await prisma.$disconnect();
        process.exit();
    });
