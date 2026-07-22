const { getSupabaseAdmin, getSupabasePublicClient, getSupabaseUserClient } = require('../config/supabase');

function normalizeEmail(value) {
    return typeof value === 'string' ? value.trim().toLowerCase() : '';
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function getDevOtpConfig() {
    if (process.env.NODE_ENV === 'production' || process.env.ENABLE_DEV_OTP !== 'true') {
        return null;
    }

    const email = normalizeEmail(process.env.DEV_OTP_TEST_EMAIL);
    const code = typeof process.env.DEV_OTP_CODE === 'string' ? process.env.DEV_OTP_CODE.trim() : '';
    const password = process.env.DEV_OTP_TEST_PASSWORD;
    if (!isValidEmail(email) || !/^\d{6}$/.test(code) || !password) {
        return null;
    }

    return { email, code, password };
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

function sessionResponse(session, user) {
    return {
        token: session.access_token,
        refreshToken: session.refresh_token,
        expiresAt: session.expires_at || null,
        user
    };
}

exports.login = async (req, res) => {
    try {
        const email = normalizeEmail(req.body.email);
        if (!isValidEmail(email)) {
            return res.status(400).json({ error: 'A valid email address is required.' });
        }

        const devOtp = getDevOtpConfig();
        if (devOtp && email === devOtp.email) {
            return res.json({ message: 'Development verification code is enabled for this test account.', email });
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

        const devOtp = getDevOtpConfig();
        if (devOtp && email === devOtp.email && otp === devOtp.code) {
            const admin = getSupabaseAdmin();
            const { data: createData, error: createError } = await admin.auth.admin.createUser({
                email,
                password: devOtp.password,
                email_confirm: true,
                user_metadata: { display_name: 'Development Test User' }
            });
            if (createError) {
                if (/already.*registered|already.*exists/i.test(createError.message)) {
                    const { data: usersData } = await admin.auth.admin.listUsers();
                    const existingUser = usersData?.users?.find(u => u.email === email);
                    if (existingUser) {
                        await admin.auth.admin.updateUserById(existingUser.id, { password: devOtp.password, email_confirm: true });
                    }
                } else {
                    throw createError;
                }
            }

            const { data: passwordSession, error: passwordError } = await getSupabasePublicClient().auth.signInWithPassword({
                email,
                password: devOtp.password
            });
            if (passwordError || !passwordSession.session || !passwordSession.user) {
                throw passwordError || new Error('Unable to create a development session.');
            }

            const user = await getAppUser(passwordSession.user, passwordSession.session.access_token);
            return res.json(sessionResponse(passwordSession.session, user));
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
        res.json(sessionResponse(data.session, user));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.refreshSession = async (req, res) => {
    try {
        const refreshToken = typeof req.body.refreshToken === 'string'
            ? req.body.refreshToken.trim()
            : '';
        if (!refreshToken) {
            return res.status(400).json({ error: 'Refresh token is required.' });
        }

        const { data, error } = await getSupabasePublicClient().auth.refreshSession({
            refresh_token: refreshToken
        });
        if (error || !data.session || !data.user) {
            return res.status(401).json({ error: error?.message || 'Invalid refresh token.' });
        }

        const user = await getAppUser(data.user, data.session.access_token);
        res.json(sessionResponse(data.session, user));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
