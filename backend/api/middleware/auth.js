const { getSupabaseAdmin } = require('../config/supabase');

module.exports = async function(req, res, next) {
    const authHeader = req.header('Authorization');
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Access denied. No token provided.' });
    }

    const token = authHeader.replace('Bearer ', '');
    try {
        const { data, error } = await getSupabaseAdmin().auth.getUser(token);
        if (error || !data.user) {
            return res.status(401).json({ error: 'Invalid or expired token.' });
        }

        req.user = { userId: data.user.id, email: data.user.email };
        req.accessToken = token;
        next();
    } catch (err) {
        res.status(401).json({ error: 'Unable to validate token.' });
    }
};
