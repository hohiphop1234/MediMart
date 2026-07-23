const express = require('express');
const cors = require('cors');
const path = require('path');
const logger = require('./middlewares/logger');

const app = express();

// Middleware
app.use(logger);
app.use(cors());
app.use(express.json());

// Admin Web Static Files
// Note: public directory is at the root, so from src/app.js we go up one level
app.use('/admin', express.static(path.join(__dirname, '../public/admin')));

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/banners', require('./routes/banners'));
app.use('/api/categories', require('./routes/categories'));
app.use('/api/products', require('./routes/products'));
app.use('/api/users', require('./routes/users'));
app.use('/api/orders', require('./routes/orders'));

// AI integration routes (/api/ocr, /api/chat)
app.use('/api', require('./routes/ai'));

// Health check route
app.get('/', (req, res) => res.json({ message: 'MediMart Supabase API is running (Prisma ORM)' }));

module.exports = app;
