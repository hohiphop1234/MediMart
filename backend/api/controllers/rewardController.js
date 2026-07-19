const Product = require('../models/Product');
const User = require('../models/User');

exports.getRewards = async (req, res) => {
    try {
        const maxPoints = req.query.maxPoints || 999999;
        const rewards = await Product.find({ isRewardItem: true, pointPrice: { $lte: maxPoints } });
        res.json(rewards);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.redeemReward = async (req, res) => {
    try {
        const { rewardId } = req.body;
        const product = await Product.findById(rewardId);
        if (!product || !product.isRewardItem) {
            return res.status(404).json({ error: 'Reward not found' });
        }

        const user = await User.findById(req.user.userId);
        if (user.loyaltyPoints < product.pointPrice) {
            return res.status(400).json({ error: 'Not enough points' });
        }

        user.loyaltyPoints -= product.pointPrice;
        await user.save();

        res.json({ success: true, remainingPoints: user.loyaltyPoints });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
