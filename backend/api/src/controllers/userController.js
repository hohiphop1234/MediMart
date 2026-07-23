const { getSupabaseUserClient } = require('../config/supabase');

function serializeProfile(profile, email) {
    return {
        _id: profile.id,
        name: profile.display_name || 'Khách hàng',
        email,
        avatarPath: profile.avatar_path || null
    };
}

function serializeAddress(address) {
    return {
        _id: address.id,
        userId: address.user_id,
        name: address.recipient_name,
        phone: address.phone,
        address: address.address_line,
        isDefault: address.is_default
    };
}

function userClient(req) {
    return getSupabaseUserClient(req.accessToken);
}

exports.getProfile = async (req, res) => {
    try {
        const { data, error } = await userClient(req)
            .from('profiles')
            .select('id, display_name, avatar_path')
            .eq('id', req.user.userId)
            .single();
        if (error) throw error;
        res.json(serializeProfile(data, req.user.email));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateProfile = async (req, res) => {
    try {
        const name = typeof req.body.name === 'string' ? req.body.name.trim() : '';
        const avatarPath = typeof req.body.avatarPath === 'string' ? req.body.avatarPath.trim() : null;
        if (!name || name.length > 100) {
            return res.status(400).json({ error: 'Name must contain 1 to 100 characters.' });
        }

        const { data, error } = await userClient(req).rpc('update_my_profile', {
            p_display_name: name,
            p_avatar_path: avatarPath
        });
        if (error) throw error;
        res.json(serializeProfile(data, req.user.email));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getAddresses = async (req, res) => {
    try {
        const { data, error } = await userClient(req)
            .from('addresses')
            .select('*')
            .order('is_default', { ascending: false })
            .order('created_at', { ascending: false });
        if (error) throw error;
        res.json(data.map(serializeAddress));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.addAddress = async (req, res) => {
    try {
        const name = typeof req.body.name === 'string' ? req.body.name.trim() : '';
        const phone = typeof req.body.phone === 'string' ? req.body.phone.trim() : '';
        const address = typeof req.body.address === 'string' ? req.body.address.trim() : '';
        const isDefault = Boolean(req.body.isDefault);
        if (!name || !phone || address.length < 8) {
            return res.status(400).json({ error: 'Name, phone, and a complete address are required.' });
        }

        const client = userClient(req);
        if (isDefault) {
            const { error: clearDefaultError } = await client
                .from('addresses')
                .update({ is_default: false })
                .eq('is_default', true);
            if (clearDefaultError) throw clearDefaultError;
        }

        const { data, error } = await client
            .from('addresses')
            .insert({
                user_id: req.user.userId,
                recipient_name: name,
                phone,
                address_line: address,
                is_default: isDefault
            })
            .select()
            .single();
        if (error) throw error;
        res.status(201).json(serializeAddress(data));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.deleteAddress = async (req, res) => {
    try {
        const { data, error } = await userClient(req)
            .from('addresses')
            .delete()
            .eq('id', req.params.id)
            .select('id');
        if (error) throw error;
        if (!data.length) return res.status(404).json({ error: 'Address not found.' });
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
