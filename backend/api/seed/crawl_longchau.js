const path = require('path');
const dotenv = require('dotenv');
dotenv.config({ path: path.resolve(__dirname, '../.env') });

const prisma = require('../src/config/prisma');

const CATEGORIES = [
    { slug: "thuoc", name: "Thuốc", icon: "https://placehold.co/100x100?text=Medicine" },
    { slug: "thuc-pham-chuc-nang", name: "Thực phẩm chức năng", icon: "https://placehold.co/100x100?text=Supplement" },
    { slug: "duoc-my-pham", name: "Dược mỹ phẩm", icon: "https://placehold.co/100x100?text=Cosmetics" },
    { slug: "cham-soc-ca-nhan", name: "Chăm sóc cá nhân", icon: "https://placehold.co/100x100?text=Personal+Care" },
    { slug: "trang-thiet-bi-y-te", name: "Thiết bị y tế", icon: "https://placehold.co/100x100?text=Medical+Device" }
];

function stripHtml(html) {
    if (!html) return "";
    return html.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ').trim();
}

const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

// Fetch and parse Next.js page data
async function fetchPageData(slug, timeoutMs = 7000) {
    const url = `https://nhathuoclongchau.com.vn/${slug}`;
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), timeoutMs);
    
    try {
        const res = await fetch(url, {
            headers: {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            },
            signal: controller.signal
        });

        if (!res.ok) {
            throw new Error(`Status: ${res.status}`);
        }

        const html = await res.text();
        const match = html.match(/<script id="__NEXT_DATA__" type="application\/json">([\s\S]*?)<\/script>/);
        if (!match) {
            throw new Error(`__NEXT_DATA__ not found`);
        }
        return JSON.parse(match[1]);
    } finally {
        clearTimeout(timeout);
    }
}

// Discover all level 2 and level 3 category slugs recursively
async function discoverCategorySlugs() {
    console.log("🔍 Scanning top-level categories to discover subcategory slugs...");
    const uniqueSlugs = new Set();

    for (const cat of CATEGORIES) {
        uniqueSlugs.add(cat.slug);
        try {
            console.log(`  Scanning: ${cat.slug}`);
            const json = await fetchPageData(cat.slug);
            const pageProps = json.props?.pageProps;
            if (!pageProps) continue;

            function scan(obj) {
                if (!obj || typeof obj !== "object") return;
                if (Array.isArray(obj)) {
                    obj.forEach(scan);
                    return;
                }
                for (const key of Object.keys(obj)) {
                    if (key === "slug" && typeof obj[key] === "string") {
                        const val = obj[key];
                        const matchesMain = CATEGORIES.some(c => val === c.slug || val.startsWith(c.slug + "/"));
                        if (matchesMain && !val.endsWith(".html")) {
                            uniqueSlugs.add(val);
                        }
                    } else {
                        scan(obj[key]);
                    }
                }
            }
            scan(json);
        } catch (err) {
            console.error(`  ⚠️ Failed to scan subcategories for ${cat.slug}:`, err.message);
        }
    }

    const slugList = Array.from(uniqueSlugs);
    console.log(`✅ Discovered ${slugList.length} unique category and subcategory URLs to crawl.`);
    return slugList;
}

// Helper to run functions in batches with delay
async function runInBatches(tasks, batchSize, delayMs) {
    const results = [];
    for (let i = 0; i < tasks.length; i += batchSize) {
        const batch = tasks.slice(i, i + batchSize);
        const batchPromises = batch.map(task => task());
        const batchResults = await Promise.all(batchPromises);
        results.push(...batchResults);
        
        if (i + batchSize < tasks.length && delayMs > 0) {
            await delay(delayMs);
        }
    }
    return results;
}

function buildFullDescription(content, fallbackName) {
    if (!content) return fallbackName;
    const parts = [];
    if (content.description) parts.push(`<h3>Mô tả sản phẩm</h3><div>${content.description}</div>`);
    if (content.ingredient) parts.push(`<h3>Thành phần</h3><div>${content.ingredient}</div>`);
    if (content.usage) parts.push(`<h3>Chỉ định / Công dụng</h3><div>${content.usage}</div>`);
    if (content.dosage) parts.push(`<h3>Liều dùng & Cách dùng</h3><div>${content.dosage}</div>`);
    if (content.adverseEffect) parts.push(`<h3>Tác dụng phụ</h3><div>${content.adverseEffect}</div>`);
    if (content.careful) parts.push(`<h3>Lưu ý / Chống chỉ định</h3><div>${content.careful}</div>`);
    if (content.preservation) parts.push(`<h3>Bảo quản</h3><div>${content.preservation}</div>`);
    return parts.length > 0 ? parts.join("\n\n") : fallbackName;
}

async function startCrawl() {
    console.log("🚀 Starting Long Chau crawler (Option B: Monograph HTML Extraction)...");
    
    let totalCrawledFromList = 0;
    let totalImported = 0;
    let totalSkippedNoPrice = 0;
    let totalSkippedDuplicate = 0;
    let totalDetailFetched = 0;
    let totalDetailFailed = 0;

    try {
        // Clear existing products in DB first to do a fresh crawl with rich HTML descriptions
        console.log("🧹 Clearing existing products from database...");
        await prisma.products.deleteMany();
        console.log("✅ Database products cleared.");

        // Resolve DB Category IDs
        const dbCategories = {};
        for (const cat of CATEGORIES) {
            let dbCat = await prisma.categories.findFirst({
                where: { name: cat.name }
            });
            if (!dbCat) {
                dbCat = await prisma.categories.create({
                    data: { name: cat.name, icon: cat.icon, product_count: 0 }
                });
                console.log(`✅ Created DB Category: ${cat.name}`);
            }
            dbCategories[cat.name.toLowerCase()] = dbCat.id;
        }

        // Discover slugs
        const slugs = await discoverCategorySlugs();

        // 1. Gather all product lists from categories
        console.log("\n--- PHASE 1: Fetching product lists from category pages ---");
        const uniqueProductsMap = new Map(); // SKU -> product info

        const listTasks = slugs.map((slug) => async () => {
            try {
                const json = await fetchPageData(slug);
                const pageProps = json.props?.pageProps;
                if (!pageProps) return;

                let productsList = [];
                if (slug === "thuoc") {
                    if (Array.isArray(pageProps.drugs)) {
                        for (const tab of pageProps.drugs) {
                            if (Array.isArray(tab.productDetails)) {
                                productsList.push(...tab.productDetails);
                            }
                        }
                    }
                } else {
                    productsList = pageProps.viewData?.products || [];
                }

                for (const prod of productsList) {
                    const productName = prod.webName || prod.name;
                    if (!productName || !prod.sku) continue;

                    // Verify exact price
                    let priceVal = null;
                    if (prod.price && typeof prod.price.price === 'number') {
                        priceVal = prod.price.price;
                    } else if (prod.price && prod.price.discount && typeof prod.price.discount.price === 'number') {
                        priceVal = prod.price.discount.price;
                    }

                    if (priceVal === null || priceVal <= 0) {
                        totalSkippedNoPrice++;
                        continue;
                    }

                    totalCrawledFromList++;
                    if (!uniqueProductsMap.has(prod.sku)) {
                        uniqueProductsMap.set(prod.sku, { ...prod, priceVal, slugContext: slug });
                    }
                }
            } catch (err) {
                console.error(`  ⚠️ Failed to fetch list for slug ${slug}:`, err.message);
            }
        });

        // Fetch lists in batches of 5
        await runInBatches(listTasks, 5, 200);
        console.log(`\nGathered ${totalCrawledFromList} products from lists.`);
        console.log(`Found ${uniqueProductsMap.size} unique products with valid prices.`);

        // 2. Filter products that don't exist in DB to avoid unnecessary detail page fetches
        console.log("\n--- PHASE 2: Filtering duplicates with DB ---");
        const productsToFetchDetails = [];

        for (const [sku, prod] of uniqueProductsMap.entries()) {
            const productName = prod.webName || prod.name;
            const existing = await prisma.products.findFirst({
                where: { name: productName }
            });

            if (existing) {
                totalSkippedDuplicate++;
            } else {
                productsToFetchDetails.push(prod);
            }
        }
        console.log(`Skipped ${totalSkippedDuplicate} duplicates already in database.`);
        console.log(`Need to fetch detailed monographs for ${productsToFetchDetails.length} new products.`);

        // 3. Fetch detailed monograph for new products
        console.log("\n--- PHASE 3: Fetching detail pages & importing products ---");
        
        const detailTasks = productsToFetchDetails.map((prod) => async () => {
            const productName = prod.webName || prod.name;
            if (!prod.slug) {
                console.log(`  Skipped (No slug): ${productName}`);
                return;
            }

            try {
                // Fetch product details
                totalDetailFetched++;
                if (totalDetailFetched % 50 === 0 || totalDetailFetched === 1) {
                    console.log(`  Fetching detail page ${totalDetailFetched}/${productsToFetchDetails.length}...`);
                }
                
                const json = await fetchPageData(prod.slug);
                const pageProps = json.props?.pageProps;
                const content = pageProps?.content;

                // Process prices
                let priceVal = prod.priceVal;
                let salePriceVal = null;
                if (prod.price && prod.price.discount && typeof prod.price.discount.finalPrice === 'number') {
                    const final = prod.price.discount.finalPrice;
                    if (final < priceVal) {
                        salePriceVal = final;
                    }
                }

                // Build combined HTML description
                const combinedHtmlDescription = buildFullDescription(content, productName);

                // Build structured monograph object
                const monographObj = content ? {
                    description: content.description || null,
                    ingredient: content.ingredient || null,
                    usage: content.usage || null,
                    dosage: content.dosage || null,
                    adverseEffect: content.adverseEffect || null,
                    careful: content.careful || null,
                    preservation: content.preservation || null
                } : null;

                // Build attributes JSON
                const attributesObj = {
                    ingredients: prod.ingredients || null,
                    dosageForm: prod.dosageForm || null,
                    specification: prod.specification || null,
                    monograph: monographObj
                };

                // Determine parent category ID
                let parentCatName = "Thuốc"; // Fallback
                if (Array.isArray(prod.category) && prod.category.length > 0) {
                    const lvl1 = prod.category.find(c => c.level === 1);
                    if (lvl1 && lvl1.name) {
                        parentCatName = lvl1.name;
                    }
                } else {
                    const firstPart = prod.slugContext.split('/')[0];
                    const matchedCat = CATEGORIES.find(c => c.slug === firstPart);
                    if (matchedCat) {
                        parentCatName = matchedCat.name;
                    }
                }

                const categoryId = dbCategories[parentCatName.toLowerCase()] || dbCategories["thuoc"];

                const isFlashSale = Math.random() < 0.15;
                const isBestSeller = Math.random() < 0.20;
                const isRewardItem = Math.random() < 0.10;

                const pointPrice = isRewardItem ? Math.round(priceVal / 100) : null;
                const flashSaleEndsAt = isFlashSale ? new Date(Date.now() + 24 * 60 * 60 * 1000) : null;

                // Create product
                await prisma.products.create({
                    data: {
                        name: productName,
                        description: combinedHtmlDescription,
                        price: priceVal,
                        sale_price: salePriceVal,
                        unit: prod.price?.measureUnitName || prod.specification?.split(' ')[0] || "Hộp",
                        image_path: prod.image || "https://placehold.co/300x300?text=No+Image",
                        brand: prod.brand || "Khác",
                        country: prod.brandOriginBadges?.attributeName || "Việt Nam",
                        category_id: categoryId,
                        is_flash_sale: isFlashSale,
                        is_best_seller: isBestSeller,
                        is_reward_item: isRewardItem,
                        point_price: pointPrice,
                        flash_sale_ends_at: flashSaleEndsAt,
                        attributes: attributesObj
                    }
                });

                totalImported++;
            } catch (err) {
                totalDetailFailed++;
                // In case of error (e.g. timeout), insert the product with fallback list description
                try {
                    let priceVal = prod.priceVal;
                    let salePriceVal = null;
                    if (prod.price && prod.price.discount && typeof prod.price.discount.finalPrice === 'number') {
                        const final = prod.price.discount.finalPrice;
                        if (final < priceVal) salePriceVal = final;
                    }
                    
                    let parentCatName = "Thuốc";
                    const firstPart = prod.slugContext.split('/')[0];
                    const matchedCat = CATEGORIES.find(c => c.slug === firstPart);
                    if (matchedCat) parentCatName = matchedCat.name;
                    const categoryId = dbCategories[parentCatName.toLowerCase()] || dbCategories["thuoc"];

                    await prisma.products.create({
                        data: {
                            name: productName,
                            description: productName,
                            price: priceVal,
                            sale_price: salePriceVal,
                            unit: prod.price?.measureUnitName || prod.specification?.split(' ')[0] || "Hộp",
                            image_path: prod.image || "https://placehold.co/300x300?text=No+Image",
                            brand: prod.brand || "Khác",
                            country: prod.brandOriginBadges?.attributeName || "Việt Nam",
                            category_id: categoryId,
                            is_flash_sale: false,
                            is_best_seller: false,
                            is_reward_item: false,
                            point_price: null,
                            flash_sale_ends_at: null,
                            attributes: {
                                ingredients: prod.ingredients || null,
                                dosageForm: prod.dosageForm || null,
                                specification: prod.specification || null,
                                monograph: null
                            }
                        }
                    });
                    totalImported++;
                } catch (dbErr) {
                    console.error(`    ❌ Double error inserting fallback for ${productName}:`, dbErr.message);
                }
            }
        });

        // Run details fetches in batches of 10 with 50ms delay
        await runInBatches(detailTasks, 10, 50);

        // 4. Update product counts
        console.log("\n📊 Updating product counts for all categories in DB...");
        for (const catName of Object.keys(dbCategories)) {
            const catId = dbCategories[catName];
            const finalProductCount = await prisma.products.count({
                where: { category_id: catId }
            });
            await prisma.categories.update({
                where: { id: catId },
                data: { product_count: finalProductCount }
            });
            console.log(`  - Category "${catName}": ${finalProductCount} products`);
        }

        console.log(`\n🎉 Crawler completed successfully!`);
        console.log(`- Total unique products imported this run: ${totalImported}`);
        console.log(`- Detail pages fetched:                  ${totalDetailFetched} (Failed: ${totalDetailFailed})`);
        console.log(`- Skipped (Prescription / No exact price):  ${totalSkippedNoPrice}`);
        console.log(`- Skipped (Duplicate products in DB):       ${totalSkippedDuplicate}`);
        
        const finalCount = await prisma.products.count();
        console.log(`- Total products in database:             ${finalCount}`);

    } catch (err) {
        console.error("❌ Crawler execution failed:", err);
    } finally {
        await prisma.$disconnect();
    }
}

startCrawl();
