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
        const categoryId = typeof req.query.categoryId === 'string'
            ? req.query.categoryId.trim()
            : '';

        if (q.length > 100) {
            return res.status(400).json({ error: 'Search query must be 100 characters or fewer' });
        }
        if (categoryId && !/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(categoryId)) {
            return res.status(400).json({ error: 'Invalid category id' });
        }

        const products = await productService.searchProducts(q, categoryId);
        res.json(products.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// --- Admin APIs ---

exports.getAllProducts = async (req, res) => {
    try {
        const products = await productService.getAllProducts();
        res.json(products); // Admin might not need strict serialization, can return full data
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getProductById = async (req, res) => {
    try {
        const product = await productService.getProductById(req.params.id);
        if (!product) return res.status(404).json({ error: 'Product not found' });
        res.json(serializeProduct(product));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createProduct = async (req, res) => {
    try {
        const newProduct = await productService.createProduct(req.body);
        res.status(201).json(newProduct);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateProduct = async (req, res) => {
    try {
        const updatedProduct = await productService.updateProduct(req.params.id, req.body);
        res.json(updatedProduct);
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
