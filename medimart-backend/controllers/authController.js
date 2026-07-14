const jwt = require('jsonwebtoken');
const User = require('../models/User');

exports.login = async (req, res) => {
    try {
        const { phone } = req.body;
        if (!phone) return res.status(400).json({ error: 'Phone number is required' });

        let user = await User.findOne({ phone });
        if (!user) {
            user = new User({ name: 'Khách hàng', phone });
            await user.save();
            const Address = require('../models/Address');
            await new Address({
                userId: user._id,
                name: user.name,
                phone: user.phone,
                address: '123 Đường Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP.HCM',
                isDefault: true
            }).save();
        }

        res.json({ message: 'OTP sent to ' + phone });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.verifyOtp = async (req, res) => {
    try {
        const { phone, otp } = req.body;
        if (!phone || !otp) return res.status(400).json({ error: 'Phone and OTP required' });

        if (otp !== '1234') {
            return res.status(400).json({ error: 'Invalid OTP' });
        }

        const user = await User.findOne({ phone });
        if (!user) return res.status(404).json({ error: 'User not found' });

        const token = jwt.sign(
            { userId: user._id, phone: user.phone },
            process.env.JWT_SECRET,
            { expiresIn: '30d' }
        );

        res.json({ token, user });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
