const mongoose = require('mongoose');
const User = require('./models/User');
const Address = require('./models/Address');
require('dotenv').config();

mongoose.connect('mongodb://127.0.0.1:27017/medimart', { useNewUrlParser: true, useUnifiedTopology: true })
    .then(async () => {
        const users = await User.find();
        for (let user of users) {
            const hasAddress = await Address.findOne({ userId: user._id });
            if (!hasAddress) {
                await new Address({
                    userId: user._id,
                    name: user.name,
                    phone: user.phone,
                    address: '123 Đường Tôn Đức Thắng, Phường Bến Nghé, Quận 1, TP.HCM',
                    isDefault: true
                }).save();
            }
        }
        console.log('Fixed addresses');
        process.exit(0);
    });
