const mongoose = require('mongoose');
const categorySchema = new mongoose.Schema({
    name:         { type: String, required: true },
    icon:         { type: String, required: true },
    productCount: { type: Number, default: 0 }
});
module.exports = mongoose.model('Category', categorySchema);
