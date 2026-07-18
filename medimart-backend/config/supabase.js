const { createClient } = require('@supabase/supabase-js');

let adminClient;

function getRequiredEnv(name) {
    const value = process.env[name];
    if (!value) {
        throw new Error(`Missing required environment variable: ${name}`);
    }
    return value;
}

function getSupabaseAdmin() {
    if (adminClient) return adminClient;

    const secretKey = process.env.SUPABASE_SECRET_KEY || process.env.SUPABASE_SERVICE_ROLE_KEY;
    if (!secretKey) {
        throw new Error('Missing required environment variable: SUPABASE_SECRET_KEY');
    }

    adminClient = createClient(getRequiredEnv('SUPABASE_URL'), secretKey, {
        auth: {
            autoRefreshToken: false,
            persistSession: false,
            detectSessionInUrl: false
        }
    });
    return adminClient;
}

function getSupabaseUserClient(accessToken) {
    return createClient(
        getRequiredEnv('SUPABASE_URL'),
        getRequiredEnv('SUPABASE_PUBLISHABLE_KEY'),
        {
            auth: {
                autoRefreshToken: false,
                persistSession: false,
                detectSessionInUrl: false
            },
            global: {
                headers: { Authorization: `Bearer ${accessToken}` }
            }
        }
    );
}

module.exports = { getSupabaseAdmin, getSupabaseUserClient };
