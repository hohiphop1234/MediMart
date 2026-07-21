const multer = require('multer');
const prisma = require('../config/prisma');

// Configure multer to store file in memory (buffer)
const upload = multer({ storage: multer.memoryStorage() });

exports.uploadMiddleware = upload.single('image');

exports.ocr = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No image provided. Please upload an image field.' });
        }

        const aiUrl = process.env.PYTHON_AI_URL || 'http://localhost:5000';
        
        // Use native FormData (available in Node 18+)
        const formData = new FormData();
        const blob = new Blob([req.file.buffer], { type: req.file.mimetype });
        formData.append('file', blob, req.file.originalname);

        // Forward request to Python backend
        const response = await fetch(`${aiUrl}/ocr`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            const errData = await response.text();
            throw new Error(`Python AI Error: ${errData}`);
        }

        const pyData = await response.json();

        const medicines = pyData.data?.medicines || [];
        const matchedMedicines = [];

        // Ensure pg_trgm extension is active (runs quickly if already exists)
        await prisma.$executeRawUnsafe('CREATE EXTENSION IF NOT EXISTS pg_trgm;');

        for (const med of medicines) {
            const medName = med.product_name;
            if (!medName) continue;

            // Try pg_trgm similarity search first
            let matches = await prisma.$queryRawUnsafe(`
                SELECT 
                    id, 
                    name, 
                    price, 
                    unit, 
                    image_path as "imagePath",
                    similarity(name, $1) as sim 
                FROM public.products 
                WHERE similarity(name, $1) > 0.08
                ORDER BY sim DESC 
                LIMIT 3
            `, medName);

            // Fallback: If similarity returns no products, do an ILIKE contains search
            if (!matches || matches.length === 0) {
                const fallbackProducts = await prisma.products.findMany({
                    where: {
                        name: {
                            contains: medName,
                            mode: 'insensitive'
                        }
                    },
                    take: 3,
                    select: {
                        id: true,
                        name: true,
                        price: true,
                        unit: true,
                        image_path: true
                    }
                });

                matches = fallbackProducts.map(p => ({
                    id: p.id,
                    name: p.name,
                    price: p.price,
                    unit: p.unit,
                    imagePath: p.image_path,
                    sim: 0.5 // Default fallback similarity score
                }));
            }

            matchedMedicines.push({
                product_name: medName,
                matches: matches.map(m => ({
                    id: m.id,
                    name: m.name,
                    price: Number(m.price || 0),
                    unit: m.unit,
                    imagePath: m.imagePath,
                    similarity: Number(m.sim || 0)
                }))
            });
        }

        // Return same structure, but medicines have matches
        res.json({
            success: true,
            data: {
                medicines: matchedMedicines,
                notes: pyData.data?.notes || ''
            },
            raw_text: pyData.raw_text || ''
        });

    } catch (err) {
        console.error('OCR Error:', err);
        res.status(500).json({ error: err.message });
    }
};

exports.chat = async (req, res) => {
    try {
        const { message, stream = true } = req.body;
        if (!message) {
            return res.status(400).json({ error: 'Message is required' });
        }

        const aiUrl = process.env.PYTHON_AI_URL || 'http://localhost:5000';

        // Forward request to Python backend
        const response = await fetch(`${aiUrl}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                messages: [{ role: 'user', content: message }],
                stream: stream
            })
        });

        if (!response.ok) {
            const errData = await response.text();
            throw new Error(`Python AI Error: ${errData}`);
        }

        if (stream) {
            // Set headers for Server-Sent Events (SSE)
            res.setHeader('Content-Type', 'text/event-stream');
            res.setHeader('Cache-Control', 'no-cache');
            res.setHeader('Connection', 'keep-alive');

            const reader = response.body.getReader();
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;
                res.write(value);
            }
            res.end();
        } else {
            const data = await response.json();
            res.json(data);
        }
    } catch (err) {
        console.error('Chat Error:', err);
        if (res.headersSent) {
            res.write(`data: ${JSON.stringify({ error: err.message })}\n\n`);
            res.end();
        } else {
            res.status(500).json({ error: err.message });
        }
    }
};
