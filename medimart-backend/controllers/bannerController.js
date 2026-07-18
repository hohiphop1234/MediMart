const { getSupabaseAdmin } = require('../config/supabase');
const { serializeBanner } = require('../utils/serializers');

exports.getBanners = async (req, res) => {
    try {
        const { data, error } = await getSupabaseAdmin()
            .from('banners')
            .select('*')
            .eq('is_active', true)
            .order('position');
        if (error) throw error;
        res.json(data.map(serializeBanner));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
