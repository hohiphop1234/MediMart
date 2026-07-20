const multer = require('multer');

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
        formData.append('image', blob, req.file.originalname);

        // Forward request to Python backend
        const response = await fetch(`${aiUrl}/ocr`, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            const errData = await response.text();
            throw new Error(`Python AI Error: ${errData}`);
        }

        const data = await response.json();
        
        // Return medicine + quantity
        res.json(data);
    } catch (err) {
        console.error('OCR Error:', err);
        res.status(500).json({ error: err.message });
    }
};

exports.chat = async (req, res) => {
    try {
        const { message } = req.body;
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
            body: JSON.stringify({ message })
        });

        if (!response.ok) {
            const errData = await response.text();
            throw new Error(`Python AI Error: ${errData}`);
        }

        const data = await response.json();
        res.json(data);
    } catch (err) {
        console.error('Chat Error:', err);
        res.status(500).json({ error: err.message });
    }
};
