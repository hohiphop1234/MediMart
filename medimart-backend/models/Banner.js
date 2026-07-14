const mongoose = require('mongoose');
const bannerSchema = new mongoose.Schema({
    imageUrl: { type: String, required: true },
    linkTo:   { type: String, default: null },
    order:    { type: Number, default: 0 }
});
module.exports = mongoose.model('Banner', bannerSchema);
