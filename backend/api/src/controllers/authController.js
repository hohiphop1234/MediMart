const { getSupabaseAdmin, getSupabaseUserClient } = require('../config/supabase');

function normalizeEmail(value) {
    return typeof value === 'string' ? value.trim().toLowerCase() : '';
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

async function getAppUser(authUser, accessToken) {
    const client = getSupabaseUserClient(accessToken);
    const { data: profile, error } = await client
        .from('profiles')
        .select('id, display_name, loyalty_points')
        .eq('id', authUser.id)
        .single();

    if (error) throw error;

    return {
        _id: profile.id,
        name: profile.display_name || 'Khách hàng',
        email: authUser.email,
        loyaltyPoints: profile.loyalty_points
    };
}

exports.login = async (req, res) => {
    try {
        const email = normalizeEmail(req.body.email);
        if (!isValidEmail(email)) {
            return res.status(400).json({ error: 'A valid email address is required.' });
        }

        const { error } = await getSupabaseAdmin().auth.signInWithOtp({
            email,
            options: {
                shouldCreateUser: true,
                data: { display_name: 'Khách hàng' }
            }
        });
        if (error) throw error;

        res.json({ message: 'A verification code has been sent.', email });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.verifyOtp = async (req, res) => {
    try {
        const email = normalizeEmail(req.body.email);
        const otp = typeof req.body.otp === 'string' ? req.body.otp.trim() : '';
        if (!isValidEmail(email) || !/^\d{6}$/.test(otp)) {
            return res.status(400).json({ error: 'A valid email and 6-digit verification code are required.' });
        }

        const { data, error } = await getSupabaseAdmin().auth.verifyOtp({
            email,
            token: otp,
            type: 'email'
        });
        if (error || !data.session || !data.user) {
            return res.status(400).json({ error: error?.message || 'Invalid or expired verification code.' });
        }

        const user = await getAppUser(data.user, data.session.access_token);
        res.json({ token: data.session.access_token, user });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
