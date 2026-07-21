require('dotenv').config();

const prisma = require('../src/config/prisma');
const { getSupabaseAdmin } = require('../src/config/supabase');

const images = [
    {
        name: 'Hỗn dịch uống men vi sinh Enterogermina Gut Defense 2 tỷ/5ml Opella (2 vỉ 10 ống)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/enterogermina_gut_defense_2_ty_2x10_ong_opella_00051091_1_ce38b37a33.jpg',
        path: 'longchau/enterogermina-gut-defense.webp'
    },
    {
        name: 'Viên uống NMN Premium 21600 Jpanwell (60 viên)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/00503274_vien_uong_chong_lao_hoa_lam_dep_da_nmn_premium_21600_60v_1865_63e4_large_5bf0c423ec.jpg',
        path: 'longchau/nmn-premium-21600.webp'
    },
    {
        name: 'Viên uống Gasso Max Vitamins For Life (30 viên)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/00345419_gasso_max_thao_duoc_ho_tro_tieu_hoa_9444_6328_large_1a28a201ca.jpg',
        path: 'longchau/gasso-max.webp'
    },
    {
        name: 'Viên uống Nano Fucoidan Biochempha (30 viên)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/DSC_09799_e7eb582916.jpg',
        path: 'longchau/nano-fucoidan-biochempha.webp'
    },
    {
        name: 'Brauer Baby & Kids Ultra Pure DHA (60 viên)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/Vien_ho_tro_phat_trien_nao_bo_suc_khoe_cho_mat_Brauer_Baby_and_Kids_Ultra_Pure_DHA_00033687_79d080f5b6.png',
        path: 'longchau/brauer-baby-kids-dha.webp'
    },
    {
        name: 'Nước Sâm Nguyên Củ Achimmadang Inbosam Biok Korea Root Drink (10 chai x 120ml)',
        sourceUrl: 'https://cdn.nhathuoclongchau.com.vn/unsafe/640x0/filters:quality(90):format(webp)/00502882_nuoc_sam_nguyen_cu_achimmadang_inbosam_biok_korea_root_dkink_10_chai_x_120ml_2001_6396_large_10523086de.jpg',
        path: 'longchau/achimmadang-inbosam.webp'
    }
];

async function importImages() {
    const storage = getSupabaseAdmin().storage.from('product-images');
    let imported = 0;

    for (const image of images) {
        const product = await prisma.products.findFirst({ where: { name: image.name } });
        if (!product) throw new Error(`Product not found: ${image.name}`);

        const response = await fetch(image.sourceUrl);
        if (!response.ok) throw new Error(`Image fetch failed (${response.status}): ${image.name}`);
        const { error } = await storage.upload(image.path, Buffer.from(await response.arrayBuffer()), {
            contentType: response.headers.get('content-type') || 'image/webp',
            cacheControl: '86400',
            upsert: true
        });
        if (error) throw error;

        await prisma.products.update({
            where: { id: product.id },
            data: {
                image_path: image.path,
                attributes: {
                    ...(product.attributes || {}),
                    imageSourceUrl: image.sourceUrl,
                    imageImportedAt: new Date().toISOString()
                }
            }
        });
        imported += 1;
    }

    console.log(JSON.stringify({ imported }));
}

importImages()
    .catch((error) => {
        console.error(error);
        process.exitCode = 1;
    })
    .finally(() => prisma.$disconnect());
