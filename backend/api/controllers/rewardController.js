const { getSupabaseUserClient } = require('../config/supabase');
const { serializeProduct } = require('../utils/serializers');

exports.getRewards = async (req, res) => {
    try {
        const rawMaxPoints = Number(req.query.maxPoints);
        const maxPoints = Number.isFinite(rawMaxPoints) && rawMaxPoints >= 0 ? rawMaxPoints : 999999;
        const { data, error } = await getSupabaseUserClient(req.accessToken)
            .from('products')
            .select('*')
            .eq('is_reward_item', true)
            .lte('point_price', maxPoints)
            .order('point_price');
        if (error) throw error;
        res.json(data.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.redeemReward = async (req, res) => {
    try {
        const productId = typeof req.body.rewardId === 'string' ? req.body.rewardId : '';
        if (!productId) return res.status(400).json({ error: 'Reward id is required.' });

        const { data, error } = await getSupabaseUserClient(req.accessToken).rpc('redeem_reward', {
            p_product_id: productId
        });
        if (error) return res.status(400).json({ error: error.message });
        res.json({ success: true, remainingPoints: data.remaining_points });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
