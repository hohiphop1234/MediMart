const { getSupabaseAdmin } = require('../config/supabase');

module.exports = async function(req, res, next) {
    const authHeader = req.header('Authorization');
    
    // Bypass authentication in development mode if token is not provided
    if (!authHeader && process.env.NODE_ENV !== 'production') {
        req.user = { userId: 'mock-dev-user-id', email: 'dev-user@medimart.com' };
        req.accessToken = 'mock-dev-token';
        return next();
    }

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        // If token is missing in production, deny access
        return res.status(401).json({ error: 'Access denied. No token provided.' });
    }

    const token = authHeader.replace('Bearer ', '');
    try {
        // Handle dev token bypass directly
        if (token === 'mock-dev-token' && process.env.NODE_ENV !== 'production') {
            req.user = { userId: 'mock-dev-user-id', email: 'dev-user@medimart.com' };
            req.accessToken = token;
            return next();
        }

        const { data, error } = await getSupabaseAdmin().auth.getUser(token);
        if (error || !data.user) {
            if (process.env.NODE_ENV !== 'production') {
                req.user = { userId: 'mock-dev-user-id', email: 'dev-user@medimart.com' };
                req.accessToken = 'mock-dev-token';
                return next();
            }
            return res.status(401).json({ error: 'Invalid or expired token.' });
        }

        req.user = { userId: data.user.id, email: data.user.email };
        req.accessToken = token;
        next();
    } catch (err) {
        if (process.env.NODE_ENV !== 'production') {
            req.user = { userId: 'mock-dev-user-id', email: 'dev-user@medimart.com' };
            req.accessToken = 'mock-dev-token';
            return next();
        }
        res.status(401).json({ error: 'Unable to validate token.' });
    }
};
