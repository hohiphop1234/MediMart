const Banner = require('../models/Banner');
exports.getBanners = async (req, res) => {
    try {
        const banners = await Banner.find().sort({ order: 1 });
        res.json(banners);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
