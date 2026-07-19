const Product = require('../models/Product');
exports.getFlashSale = async (req, res) => {
    try {
        const products = await Product.find({ isFlashSale: true });
        const endTime = new Date();
        endTime.setHours(23, 59, 59, 999);
        res.json({ products, endTime: endTime.toISOString() });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getBestSellers = async (req, res) => {
    try {
        const products = await Product.find({ isBestSeller: true });
        res.json(products);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.searchProducts = async (req, res) => {
    try {
        const q = req.query.q || '';
        const products = await Product.find({ name: { $regex: q, $options: 'i' } });
        res.json(products);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
