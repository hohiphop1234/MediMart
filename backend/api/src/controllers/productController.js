const productService = require('../services/productService');
const { serializeProduct } = require('../utils/serializers');

function endOfToday() {
    const endTime = new Date();
    endTime.setHours(23, 59, 59, 999);
    return endTime.toISOString();
}

exports.getFlashSale = async (req, res) => {
    try {
        const products = await productService.getFlashSale();
        const scheduledEndTimes = products
            .map((product) => product.flash_sale_ends_at)
            .filter(Boolean)
            .sort();
        res.json({
            products: products.map(serializeProduct),
            endTime: scheduledEndTimes[0] || endOfToday()
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getBestSellers = async (req, res) => {
    try {
        const products = await productService.getBestSellers();
        res.json(products.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.searchProducts = async (req, res) => {
    try {
        const q = typeof req.query.q === 'string' ? req.query.q.trim() : '';
        const products = await productService.searchProducts(q);
        res.json(products.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// --- Admin APIs ---

function formatAdminProduct(p) {
    return {
        _id: p.id,
        id: p.id,
        name: p.name,
        description: p.description || '',
        price: Number(p.price),
        salePrice: p.sale_price !== null && p.sale_price !== undefined ? Number(p.sale_price) : null,
        unit: p.unit || '',
        imagePath: p.image_path || '',
        imageUrl: p.image_path || '',
        categoryId: p.category_id,
        category_id: p.category_id,
        Category: p.categories ? { _id: p.categories.id, id: p.categories.id, name: p.categories.name } : null,
        categories: p.categories,
        brand: p.brand || '',
        country: p.country || '',
        isFlashSale: Boolean(p.is_flash_sale),
        isBestSeller: Boolean(p.is_best_seller),
        isRewardItem: Boolean(p.is_reward_item),
        pointPrice: p.point_price
    };
}

exports.getAllProducts = async (req, res) => {
    try {
        const products = await productService.getAllProducts();
        res.json(products.map(formatAdminProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getProductById = async (req, res) => {
    try {
        const product = await productService.getProductById(req.params.id);
        if (!product) return res.status(404).json({ error: 'Product not found' });
        res.json(formatAdminProduct(product));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createProduct = async (req, res) => {
    try {
        const newProduct = await productService.createProduct(req.body);
        res.status(201).json(formatAdminProduct(newProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateProduct = async (req, res) => {
    try {
        const updatedProduct = await productService.updateProduct(req.params.id, req.body);
        res.json(formatAdminProduct(updatedProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.deleteProduct = async (req, res) => {
    try {
        await productService.deleteProduct(req.params.id);
        res.json({ message: 'Product deleted successfully' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// --- Internal API for Python ---

exports.getSimilarProducts = async (req, res) => {
    try {
        const { medicineName } = req.body;
        if (!medicineName) {
            return res.status(400).json({ error: 'medicineName is required' });
        }
        const products = await productService.getSimilarProducts(medicineName);
        res.json(products.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
