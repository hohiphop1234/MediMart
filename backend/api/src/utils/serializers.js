const { getSupabaseAdmin } = require('../config/supabase');

function publicImageUrl(bucket, path) {
    if (typeof path !== 'string' || !path.trim()) return null;

    const normalizedPath = path.trim();
    if (/^https?:\/\//i.test(normalizedPath)) return normalizedPath;

    return getSupabaseAdmin().storage.from(bucket).getPublicUrl(normalizedPath).data.publicUrl;
}

function serializeProduct(product) {
    return {
        _id: product.id,
        name: product.name,
        description: product.description || '',
        price: Number(product.price),
        salePrice: product.sale_price === null ? null : Number(product.sale_price),
        unit: product.unit || '',
        imageUrl: publicImageUrl('product-images', product.image_path) || '',
        categoryId: product.category_id,
        brand: product.brand || '',
        country: product.country || '',
        isFlashSale: product.is_flash_sale,
        isBestSeller: product.is_best_seller,
        isRewardItem: product.is_reward_item,
        pointPrice: product.point_price,
        attributes: product.attributes || {}
    };
}

function serializeCategory(category) {
    return {
        _id: category.id,
        name: category.name,
        icon: category.icon || '',
        productCount: category._count?.products ?? category.product_count
    };
}

function serializeBanner(banner) {
    return {
        _id: banner.id,
        imageUrl: publicImageUrl('banner-images', banner.image_path) || '',
        linkTo: banner.link_to || ''
    };
}

module.exports = { serializeProduct, serializeCategory, serializeBanner };
