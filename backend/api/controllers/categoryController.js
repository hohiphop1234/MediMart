const { getSupabaseAdmin } = require('../config/supabase');
const { serializeCategory } = require('../utils/serializers');

exports.getCategories = async (req, res) => {
    try {
        const { data, error } = await getSupabaseAdmin()
            .from('categories')
            .select('*')
            .order('name');
        if (error) throw error;
        res.json(data.map(serializeCategory));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
