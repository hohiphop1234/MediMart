const { getSupabaseAdmin } = require('../config/supabase');
const { serializeProduct } = require('../utils/serializers');

function endOfToday() {
    const endTime = new Date();
    endTime.setHours(23, 59, 59, 999);
    return endTime.toISOString();
}

exports.getFlashSale = async (req, res) => {
    try {
        const { data, error } = await getSupabaseAdmin()
            .from('products')
            .select('*')
            .eq('is_flash_sale', true)
            .order('created_at', { ascending: false });
        if (error) throw error;

        const scheduledEndTimes = data
            .map((product) => product.flash_sale_ends_at)
            .filter(Boolean)
            .sort();
        res.json({
            products: data.map(serializeProduct),
            endTime: scheduledEndTimes[0] || endOfToday()
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getBestSellers = async (req, res) => {
    try {
        const { data, error } = await getSupabaseAdmin()
            .from('products')
            .select('*')
            .eq('is_best_seller', true)
            .order('created_at', { ascending: false });
        if (error) throw error;
        res.json(data.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.searchProducts = async (req, res) => {
    try {
        const q = typeof req.query.q === 'string' ? req.query.q.trim() : '';
        let query = getSupabaseAdmin().from('products').select('*').order('created_at', { ascending: false });
        if (q) query = query.ilike('name', `%${q}%`);

        const { data, error } = await query;
        if (error) throw error;
        res.json(data.map(serializeProduct));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
