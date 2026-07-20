const API_BASE = '/api';

// --- State ---
let products = [];
let categories = [];
let orders = [];

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initTheme();
    loadDashboardData();
    
    // Form Submissions
    document.getElementById('product-form').addEventListener('submit', handleProductSubmit);
    document.getElementById('category-form').addEventListener('submit', handleCategorySubmit);
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
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><img src="${p.imagePath || 'https://via.placeholder.com/48'}" alt="Image"></td>
            <td><strong>${p.name}</strong></td>
            <td>${p.Category?.name || '-'}</td>
            <td>${p.price.toLocaleString()} ₫</td>
            <td>
                ${p.isFlashSale ? '<span class="status-badge status-shipping">Flash Sale</span>' : ''}
                ${p.isBestSeller ? '<span class="status-badge status-delivered">Best Seller</span>' : ''}
            </td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon btn-edit" onclick="editProduct('${p._id}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon btn-delete" onclick="deleteProduct('${p._id}')"><i class="fa-solid fa-trash"></i></button>
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
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><img src="${c.icon || 'https://via.placeholder.com/48'}" alt="Icon"></td>
            <td><strong>${c.name}</strong></td>
            <td>${c.productCount || 0}</td>
            <td>
                <div class="action-btns">
                    <button class="btn-icon btn-edit" onclick="editCategory('${c._id}')"><i class="fa-solid fa-pen"></i></button>
                    <button class="btn-icon btn-delete" onclick="deleteCategory('${c._id}')"><i class="fa-solid fa-trash"></i></button>
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
        const date = new Date(o.createdAt).toLocaleDateString();
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${o._id.substring(0, 8)}</strong><br><small>${date}</small></td>
            <td>${o.User?.email || o.User?.id || 'Unknown'}</td>
            <td><strong>${o.totalAmount.toLocaleString()} ₫</strong></td>
            <td>${o.paymentMethod}</td>
            <td>
                <select class="status-select status-${o.status.toLowerCase()}" onchange="updateOrderStatus('${o._id}', this.value)">
                    <option value="PENDING" ${o.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                    <option value="SHIPPING" ${o.status === 'SHIPPING' ? 'selected' : ''}>SHIPPING</option>
                    <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                    <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
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
        const opt = document.createElement('option');
        opt.value = c._id;
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
    const p = products.find(x => x._id === id);
    if (!p) return;

    document.getElementById('product-id').value = p._id;
    document.getElementById('product-name').value = p.name;
    document.getElementById('product-category').value = p.categoryId || '';
    document.getElementById('product-price').value = p.price;
    document.getElementById('product-image').value = p.imagePath || '';
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
