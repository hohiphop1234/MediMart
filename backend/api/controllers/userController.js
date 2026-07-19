const User = require('../models/User');
const Address = require('../models/Address');

exports.getProfile = async (req, res) => {
    try {
        const user = await User.findById(req.user.userId);
        res.json(user);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateProfile = async (req, res) => {
    try {
        const user = await User.findByIdAndUpdate(req.user.userId, req.body, { new: true });
        res.json(user);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getMyPoints = async (req, res) => {
    try {
        const user = await User.findById(req.user.userId);
        res.json({ points: user.loyaltyPoints });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getAddresses = async (req, res) => {
    try {
        const addresses = await Address.find({ userId: req.user.userId });
        res.json(addresses);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.addAddress = async (req, res) => {
    try {
        const address = new Address({ userId: req.user.userId, ...req.body });
        await address.save();
        res.json(address);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.deleteAddress = async (req, res) => {
    try {
        await Address.findOneAndDelete({ _id: req.params.id, userId: req.user.userId });
        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
