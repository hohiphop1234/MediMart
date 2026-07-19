const mongoose = require('mongoose');
const orderSchema = new mongoose.Schema({
    userId:        { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    items: [{
        productId: { type: mongoose.Schema.Types.ObjectId, ref: 'Product' },
        name:      String,
        quantity:   Number,
        price:     Number
    }],
    totalAmount:   { type: Number, required: true },
    status:        { type: String, enum: ['PENDING', 'SHIPPING', 'DELIVERED', 'RETURNED'], default: 'PENDING' },
    addressId:     { type: mongoose.Schema.Types.ObjectId, ref: 'Address' },
    paymentMethod: { type: String, default: 'COD' },
    createdAt:     { type: Date, default: Date.now }
});
module.exports = mongoose.model('Order', orderSchema);
