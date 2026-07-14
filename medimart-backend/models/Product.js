const mongoose = require('mongoose');
const productSchema = new mongoose.Schema({
    name:         { type: String, required: true },
    description:  { type: String },
    price:        { type: Number, required: true },
    salePrice:    { type: Number, default: null },
    unit:         { type: String, required: true },
    imageUrl:     { type: String, required: true },
    categoryId:   { type: mongoose.Schema.Types.ObjectId, ref: 'Category' },
    brand:        { type: String },
    country:      { type: String },
    isFlashSale:  { type: Boolean, default: false },
    isBestSeller: { type: Boolean, default: false },
    isRewardItem: { type: Boolean, default: false },
    pointPrice:   { type: Number, default: null }
});
module.exports = mongoose.model('Product', productSchema);
