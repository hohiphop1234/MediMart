const API_BASE = '/api';

// --- State ---
let products = [];
let categories = [];
let orders = [];

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    // Configure marked options for line breaks
    if (window.marked) {
        marked.use({
            breaks: true,
            gfm: true
        });
    }

    initNavigation();
    initTheme();
    loadDashboardData();
    
    // Form Submissions
    document.getElementById('product-form').addEventListener('submit', handleProductSubmit);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
    
    // Initialize AI Feature components
    loadSavedToken();
    initOCRUploadZone();
});

// --- Navigation ---
function initNavigation() {
    const links = document.querySelectorAll('.nav-links li');
    const sections = document.querySelectorAll('.content-section');

    links.forEach(link => {
        link.addEventListener('click', () => {
            // Remove active from all links and sections
            links.forEach(l => l.classList.remove('active'));
            sections.forEach(s => s.classList.remove('active'));

            // Add active to clicked link and corresponding section
            link.classList.add('active');
            const targetId = link.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');

            // Load specific data if needed
            if (targetId === 'products-section') loadProducts();
            if (targetId === 'categories-section') loadCategories();
            if (targetId === 'orders-section') loadOrders();
            if (targetId === 'dashboard-section') loadDashboardData();
        });
    });
}

// --- Theme Toggle ---
function initTheme() {
    const toggleBtn = document.getElementById('theme-toggle');
    const icon = toggleBtn.querySelector('i');
    
    // Check saved theme
    if (localStorage.getItem('theme') === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        icon.classList.replace('fa-moon', 'fa-sun');
    }

    toggleBtn.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        if (currentTheme === 'dark') {
            document.documentElement.removeAttribute('data-theme');
            localStorage.setItem('theme', 'light');
            icon.classList.replace('fa-sun', 'fa-moon');
        } else {
            document.documentElement.setAttribute('data-theme', 'dark');
            localStorage.setItem('theme', 'dark');
            icon.classList.replace('fa-moon', 'fa-sun');
        }
    });
}

// --- Dashboard ---
async function loadDashboardData() {
    try {
        const [pRes, cRes, oRes] = await Promise.all([
            fetch(`${API_BASE}/products`),
            fetch(`${API_BASE}/categories`),
            fetch(`${API_BASE}/orders`)
        ]);

        products = await pRes.json();
        categories = await cRes.json();
        orders = await oRes.json();

        document.getElementById('total-products-count').textContent = products.length || 0;
        document.getElementById('total-categories-count').textContent = categories.length || 0;
        document.getElementById('total-orders-count').textContent = orders.length || 0;
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

// --- Products ---
async function loadProducts() {
    try {
        const res = await fetch(`${API_BASE}/products`);
        products = await res.json();
        
        // Also ensure categories are loaded for the form
        if (categories.length === 0) {
            const cRes = await fetch(`${API_BASE}/categories`);
            categories = await cRes.json();
        }

        renderProductsTable();
        populateCategorySelect();
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

function renderProductsTable() {
    const tbody = document.querySelector('#products-table tbody');
    tbody.innerHTML = '';

    products.forEach(p => {
        const id = p._id || p.id;
        const img = p.imagePath || p.image_path || p.imageUrl || 'https://via.placeholder.com/48';
        const categoryName = p.Category?.name || p.categories?.name || '-';
        const price = Number(p.price || 0).toLocaleString();
        const isFlashSale = p.isFlashSale || p.is_flash_sale;
        const isBestSeller = p.isBestSeller || p.is_best_seller;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><img src="${img}" alt="Image"></td>
            <td><strong>${p.name}</strong></td>
            <td>${categoryName}</td>
            <td>${price} ₫</td>
            <td>
                ${isFlashSale ? '<span class="status-badge status-shipping">Flash Sale</span>' : ''}
                ${isBestSeller ? '<span class="status-badge status-delivered">Best Seller</span>' : ''}
            </td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon btn-edit" onclick="editProduct('${id}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon btn-delete" onclick="deleteProduct('${id}')"><i class="fa-solid fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// --- Categories ---
async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/categories`);
        categories = await res.json();
        renderCategoriesTable();
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function renderCategoriesTable() {
    const tbody = document.querySelector('#categories-table tbody');
    tbody.innerHTML = '';

    categories.forEach(c => {
        const id = c._id || c.id;
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><img src="${c.icon || 'https://via.placeholder.com/48'}" alt="Icon"></td>
            <td><strong>${c.name}</strong></td>
            <td>${c.productCount || c.product_count || 0}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon btn-edit" onclick="editCategory('${id}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon btn-delete" onclick="deleteCategory('${id}')"><i class="fa-solid fa-trash"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// --- Orders ---
async function loadOrders() {
    try {
        const res = await fetch(`${API_BASE}/orders`);
        orders = await res.json();
        renderOrdersTable();
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

function renderOrdersTable() {
    const tbody = document.querySelector('#orders-table tbody');
    tbody.innerHTML = '';

    orders.forEach(o => {
        const id = o._id || o.id;
        const date = new Date(o.createdAt || o.created_at).toLocaleDateString();
        const userDisplay = o.User?.email || o.users?.phone || o.users?.id || o.user_id || 'Unknown';
        const total = Number(o.totalAmount || o.total_amount || 0).toLocaleString();
        const payment = o.paymentMethod || o.payment_method || 'COD';
        const status = o.status || 'PENDING';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${id.substring(0, 8)}</strong><br><small>${date}</small></td>
            <td>${userDisplay}</td>
            <td><strong>${total} ₫</strong></td>
            <td>${payment}</td>
            <td>
                <select class="status-select status-${status.toLowerCase()}" onchange="updateOrderStatus('${id}', this.value)">
                    <option value="PENDING" ${status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="SHIPPING" ${status === 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
                    <option value="DELIVERED" ${status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                    <option value="CANCELLED" ${status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                </select>
            </td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon btn-edit" onclick="alert('View details not implemented yet')"><i class="fa-solid fa-eye"></i></button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function updateOrderStatus(orderId, status) {
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status })
        });
        if (res.ok) {
            loadOrders(); // Reload to refresh colors
        } else {
            alert('Failed to update status');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// --- Modals & Forms ---
function closeModals() {
    document.getElementById('product-modal').classList.add('hidden');
    document.getElementById('category-modal').classList.add('hidden');
}

function populateCategorySelect() {
    const select = document.getElementById('product-category');
    select.innerHTML = '<option value="">Select Category</option>';
    categories.forEach(c => {
        const id = c._id || c.id;
        const opt = document.createElement('option');
        opt.value = id;
        opt.textContent = c.name;
        select.appendChild(opt);
    });
}

// Product CRUD
function openProductModal() {
    document.getElementById('product-form').reset();
    document.getElementById('product-id').value = '';
    document.getElementById('product-modal-title').textContent = 'Add New Product';
    document.getElementById('product-modal').classList.remove('hidden');
}

function editProduct(id) {
    const p = products.find(x => (x._id || x.id) === id);
    if (!p) return;

    document.getElementById('product-id').value = p._id || p.id;
    document.getElementById('product-name').value = p.name || '';
    document.getElementById('product-category').value = p.categoryId || p.category_id || '';
    document.getElementById('product-price').value = p.price || 0;
    document.getElementById('product-image').value = p.imagePath || p.image_path || p.imageUrl || '';
    document.getElementById('product-unit').value = p.unit || '';
    document.getElementById('product-desc').value = p.description || '';
    
    document.getElementById('product-modal-title').textContent = 'Edit Product';
    document.getElementById('product-modal').classList.remove('hidden');
}

async function handleProductSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('product-id').value;
    const payload = {
        name: document.getElementById('product-name').value,
        categoryId: document.getElementById('product-category').value,
        price: document.getElementById('product-price').value,
        imagePath: document.getElementById('product-image').value,
        unit: document.getElementById('product-unit').value,
        description: document.getElementById('product-desc').value,
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API_BASE}/products/${id}` : `${API_BASE}/products`;

    try {
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            closeModals();
            loadProducts();
            loadDashboardData();
        } else {
            alert('Failed to save product');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
        const res = await fetch(`${API_BASE}/products/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadProducts();
            loadDashboardData();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// Category CRUD
function openCategoryModal() {
    document.getElementById('category-form').reset();
    document.getElementById('category-id').value = '';
    document.getElementById('category-modal-title').textContent = 'Add New Category';
    document.getElementById('category-modal').classList.remove('hidden');
}

function editCategory(id) {
    const c = categories.find(x => x._id === id);
    if (!c) return;

    document.getElementById('category-id').value = c._id;
    document.getElementById('category-name').value = c.name;
    document.getElementById('category-icon').value = c.icon || '';
    
    document.getElementById('category-modal-title').textContent = 'Edit Category';
    document.getElementById('category-modal').classList.remove('hidden');
}

async function handleCategorySubmit(e) {
    e.preventDefault();
    const id = document.getElementById('category-id').value;
    const payload = {
        name: document.getElementById('category-name').value,
        icon: document.getElementById('category-icon').value,
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API_BASE}/categories/${id}` : `${API_BASE}/categories`;

    try {
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            closeModals();
            loadCategories();
            loadDashboardData();
        } else {
            alert('Failed to save category');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

async function deleteCategory(id) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    try {
        const res = await fetch(`${API_BASE}/categories/${id}`, { method: 'DELETE' });
        if (res.ok) {
            loadCategories();
            loadDashboardData();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

// --- AI Features ---

// Auth Token management
function getAuthHeaders() {
    const token = localStorage.getItem('supabase_auth_token');
    const headers = {};
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

function saveAuthToken() {
    const token = document.getElementById('auth-token-input').value.trim();
    const status = document.getElementById('token-status');
    if (!token) {
        status.textContent = 'Please enter a token first.';
        status.className = 'token-status-msg error';
        return;
    }
    localStorage.setItem('supabase_auth_token', token);
    status.textContent = 'Token saved successfully!';
    status.className = 'token-status-msg success';
}

function clearAuthToken() {
    localStorage.removeItem('supabase_auth_token');
    document.getElementById('auth-token-input').value = '';
    const status = document.getElementById('token-status');
    status.textContent = 'Token cleared.';
    status.className = 'token-status-msg error';
}

function loadSavedToken() {
    const token = localStorage.getItem('supabase_auth_token');
    const status = document.getElementById('token-status');
    if (token) {
        document.getElementById('auth-token-input').value = token;
        status.textContent = 'Saved token loaded.';
        status.className = 'token-status-msg success';
    }
}

// Chatbot functionality
async function sendChatMessage() {
    const input = document.getElementById('chat-user-input');
    const messageText = input.value.trim();
    if (!messageText) return;

    // Clear input
    input.value = '';

    // Append user message
    appendChatMessage('user', messageText);

    // Append loading message
    const loadingId = appendChatMessage('assistant', 'Thinking...', true);

    try {
        const headers = getAuthHeaders();

        const res = await fetch(`${API_BASE}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...headers
            },
            body: JSON.stringify({ message: messageText, stream: true })
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            const errMsg = errData.error || `HTTP ${res.status}`;
            updateChatMessageContent(loadingId, `Error calling API: ${errMsg}`);
            return;
        }

        // Clear the "Thinking..." loader content
        const el = document.getElementById(loadingId);
        if (el) {
            const contentDiv = el.querySelector('.message-content');
            if (contentDiv) contentDiv.innerHTML = '';
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let fullReply = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            
            const chunkText = decoder.decode(value);
            const lines = chunkText.split('\n');
            for (const line of lines) {
                const cleanLine = line.trim();
                if (!cleanLine) continue;
                if (cleanLine.startsWith('data: ')) {
                    const dataStr = cleanLine.substring(6).trim();
                    if (dataStr === '[DONE]') {
                        break;
                    }
                    try {
                        const parsed = JSON.parse(dataStr);
                        const content = parsed.choices?.[0]?.delta?.content || '';
                        if (content) {
                            fullReply += content;
                            updateChatMessageContent(loadingId, fullReply);
                        }
                    } catch (e) {
                        // Suppress parse errors for partial chunks
                    }
                }
            }
        }
    } catch (error) {
        console.error('Chat error:', error);
        updateChatMessageContent(loadingId, `Network error: ${error.message}`);
    }
}

function appendChatMessage(role, content, isLoading = false) {
    const container = document.getElementById('chat-messages-container');
    const msgId = 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    
    const div = document.createElement('div');
    div.className = `message ${role}-message`;
    div.id = msgId;
    
    if (isLoading) {
        div.innerHTML = `<div class="message-content"><i class="fa-solid fa-spinner fa-spin"></i> <span>${content}</span></div>`;
    } else {
        const formatted = (role === 'assistant' && window.marked) ? marked.parse(content) : `<pre>${escapeHTML(content)}</pre>`;
        div.innerHTML = `<div class="message-content">${formatted}</div>`;
    }
    
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    return msgId;
}

function updateChatMessageContent(msgId, content) {
    const el = document.getElementById(msgId);
    if (el) {
        const contentDiv = el.querySelector('.message-content');
        if (contentDiv) {
            const formatted = window.marked ? marked.parse(content) : `<pre>${escapeHTML(content)}</pre>`;
            contentDiv.innerHTML = formatted;
        }
        const container = document.getElementById('chat-messages-container');
        container.scrollTop = container.scrollHeight;
    }
}

function escapeHTML(text) {
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// OCR functionality
let selectedOCRFile = null;

function initOCRUploadZone() {
    const zone = document.getElementById('ocr-upload-zone');
    const fileInput = document.getElementById('ocr-file-input');

    if (!zone || !fileInput) return;

    zone.addEventListener('click', () => fileInput.click());

    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleOCRFileSelect(e.target.files[0]);
        }
    });

    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        zone.classList.add('dragover');
    });

    zone.addEventListener('dragleave', () => {
        zone.classList.remove('dragover');
    });

    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('dragover');
        if (e.dataTransfer.files.length > 0) {
            handleOCRFileSelect(e.dataTransfer.files[0]);
        }
    });
}

function handleOCRFileSelect(file) {
    if (!file.type.startsWith('image/')) {
        alert('Please upload an image file (PNG/JPEG).');
        return;
    }
    selectedOCRFile = file;

    // Show preview
    const reader = new FileReader();
    reader.onload = (e) => {
        document.getElementById('ocr-image-preview').src = e.target.result;
        document.getElementById('ocr-upload-zone').classList.add('hidden');
        document.getElementById('ocr-preview-area').classList.remove('hidden');
        document.getElementById('btn-run-ocr').disabled = false;
    };
    reader.readAsDataURL(file);
}

function removeOCRImage() {
    selectedOCRFile = null;
    document.getElementById('ocr-file-input').value = '';
    document.getElementById('ocr-image-preview').src = '';
    document.getElementById('ocr-upload-zone').classList.remove('hidden');
    document.getElementById('ocr-preview-area').classList.add('hidden');
    document.getElementById('btn-run-ocr').disabled = true;
    document.getElementById('ocr-results-area').classList.add('hidden');
}

async function runOCR() {
    if (!selectedOCRFile) return;

    const btn = document.getElementById('btn-run-ocr');
    const origHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';

    // Reset results area
    document.getElementById('ocr-results-area').classList.add('hidden');
    const tbody = document.getElementById('ocr-medicines-tbody');
    tbody.innerHTML = '';
    document.getElementById('ocr-notes-text').textContent = '-';
    document.getElementById('ocr-raw-pre').textContent = '';

    const formData = new FormData();
    formData.append('image', selectedOCRFile);

    try {
        const headers = getAuthHeaders();

        const res = await fetch(`${API_BASE}/ocr`, {
            method: 'POST',
            headers: {
                ...headers
            },
            body: formData
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            const errMsg = errData.error || `HTTP ${res.status}`;
            alert(`OCR Parsing Failed: ${errMsg}`);
            btn.disabled = false;
            btn.innerHTML = origHtml;
            return;
        }

        const data = await res.json();
        renderOCRResults(data);
    } catch (error) {
        console.error('OCR API error:', error);
        alert(`Network Error: ${error.message}`);
    } finally {
        btn.disabled = false;
        btn.innerHTML = origHtml;
    }
}

function renderOCRResults(data) {
    document.getElementById('ocr-results-area').classList.remove('hidden');

    // Render raw OCR text
    document.getElementById('ocr-raw-pre').textContent = data.raw_text || 'No raw text extracted.';

    // Render structured medicine list
    const tbody = document.getElementById('ocr-medicines-tbody');
    tbody.innerHTML = '';

    const medicines = data.data?.medicines || [];
    if (medicines.length === 0) {
        tbody.innerHTML = '<tr><td colspan="2" style="text-align: center; color: var(--text-muted);">No structured medicines found. Check Raw Text tab.</td></tr>';
    } else {
        medicines.forEach(m => {
            const tr = document.createElement('tr');
            
            // Build matches HTML
            let matchesHtml = '<div class="ocr-match-list">';
            const matches = m.matches || [];
            if (matches.length === 0) {
                matchesHtml += `<span style="color: var(--text-muted); font-style: italic;">No matching products found in database</span>`;
            } else {
                matches.forEach(match => {
                    const img = match.imagePath || 'https://via.placeholder.com/36';
                    const price = Number(match.price).toLocaleString();
                    const simPercent = Math.round(match.similarity * 100);
                    matchesHtml += `
                        <div class="ocr-match-item">
                            <img src="${img}" class="ocr-match-img" onerror="this.src='https://via.placeholder.com/36'">
                            <div class="ocr-match-details">
                                <span class="ocr-match-name"><strong>${escapeHTML(match.name)}</strong> (${escapeHTML(match.unit || 'Đơn vị')})</span>
                                <span class="ocr-match-price">${price} ₫</span>
                            </div>
                            <span class="status-badge status-delivered ocr-match-badge">${simPercent}% Match</span>
                        </div>
                    `;
                });
            }
            matchesHtml += '</div>';

            tr.innerHTML = `
                <td style="vertical-align: top; width: 40%;"><strong>${escapeHTML(m.product_name)}</strong></td>
                <td>${matchesHtml}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    // Render notes
    document.getElementById('ocr-notes-text').textContent = data.data?.notes || 'No extra notes parsed.';
}

function switchOCRTab(event, tabId) {
    const panel = event.target.closest('.ocr-panel');
    panel.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    panel.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

    event.target.classList.add('active');
    document.getElementById(tabId).classList.add('active');
}
