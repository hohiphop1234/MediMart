# MediMart Node API Backend

Đây là dịch vụ backend Web API chính cho dự án **MediMart**, được xây dựng trên nền tảng **Node.js (Express)** và **Prisma ORM**, kết nối trực tiếp đến cơ sở dữ liệu Supabase PostgreSQL.

Dịch vụ này cung cấp các endpoint API cho ứng dụng di động Android (như quản lý sản phẩm, đơn hàng, người dùng, điểm thưởng, và kết nối với AI Backend).

---

## 🛠️ Công Nghệ Sử Dụng

1. **Node.js (Express)**: Cung cấp RESTful API cổng `3000`.
2. **Prisma ORM**: Quản lý schema, sinh mã nguồn truy vấn kiểu an toàn (Prisma Client) và thực hiện các câu lệnh database migration.
3. **Supabase**: 
   - **Postgres Database**: Lưu trữ dữ liệu hệ thống (người dùng, sản phẩm, danh mục, đơn hàng...).
   - **GoTrue Auth**: Quản lý xác thực JWT token truyền từ ứng dụng di động.
   - **Storage**: Lưu trữ hình ảnh sản phẩm hoặc đơn thuốc tải lên.

---

## 🚀 Hướng Dẫn Cài Đặt

### 1. Cài Đặt Thư Viện
Truy cập vào thư mục `/backend/api` và chạy lệnh cài đặt:
```bash
npm install
```

### 2. Cấu Hình Biến Môi Trường (.env)
Sao chép tệp `.env.example` thành `.env`:
```bash
cp .env.example .env
```
Thiết lập các biến môi trường sau từ trang quản trị Supabase (Dashboard -> Settings -> API):
```env
PORT=3000
DATABASE_URL="postgresql://postgres.[your-id]:[password]@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?pgbouncer=true"
DIRECT_URL="postgresql://postgres.[your-id]:[password]@aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres"
SUPABASE_URL="https://[your-id].supabase.co"
SUPABASE_PUBLISHABLE_KEY="[your-anon-key]"
SUPABASE_SECRET_KEY="[your-service-role-key]"
```
> [!IMPORTANT]
> Khóa `SUPABASE_SECRET_KEY` (Service Role Key) chỉ được đặt ở backend Node và tuyệt đối không được đưa vào mã nguồn Android hoặc commit lên Git.

---

## 🗄️ Quản Lý Database (Prisma)

### 1. Đồng Bộ Hóa Schema Lên Database
Để đẩy các thay đổi trong schema Prisma lên Supabase PostgreSQL:
```bash
npx prisma db push
```

### 2. Khởi Tạo Prisma Client
Sau khi thay đổi schema hoặc cài đặt mới, sinh lại mã nguồn truy vấn:
```bash
npx prisma generate
```

---

## 🌿 Seeding & Crawling Dữ Liệu Sản Phẩm

Dự án hỗ trợ 2 công cụ nạp dữ liệu (seed) sản phẩm vào database:

### 1. Chạy Seed Mặc Định (Dữ Liệu Demo)
Để tạo các danh mục mặc định và dữ liệu sản phẩm demo cơ bản:
```bash
npm run seed
```

### 2. Chạy Crawler Long Châu (Dữ Dòng Thực Tế)
Để tự động quét và nạp dữ liệu thực tế từ hệ thống Nhà Thuốc FPT Long Châu:
```bash
npm run crawl:longchau
```

**Tính năng nổi bật của Crawler:**
- **Tìm kiếm danh mục đệ quy**: Tự động phát hiện toàn bộ **61 liên kết danh mục và phụ danh mục** từ trang chủ.
- **Giá bán chính xác 100%**: Kiểm tra kỹ lưỡng và chỉ nạp các sản phẩm có hiển thị giá công khai (bỏ qua thuốc kê đơn không có giá niêm yết).
- **Trích xuất thông tin chi tiết (Monograph)**:
  - Tải trang chi tiết sản phẩm và phân tích mã nguồn `__NEXT_DATA__` của Next.js.
  - Gộp các phần thành một trang HTML đầy đủ định dạng (`Mô tả`, `Thành phần`, `Chỉ định`, `Cách dùng`, `Tác dụng phụ`, `Lưu ý`, `Bảo quản`) và lưu vào trường `description` để các thành phần WebView hoặc `Html.fromHtml` trên ứng dụng di động hiển thị dạng Rich Text.
  - Tách lẻ các mục HTML lưu vào đối tượng JSON `attributes.monograph` để ứng dụng di động có thể hiển thị dạng **Tab** hoặc giao diện co giãn (**Accordion**) trực quan.
- **Chạy Polite Batch**: Xử lý đồng thời 5-10 request với độ trễ ngắn nhằm tuân thủ nguyên tắc cào dữ liệu lịch sự, tránh quá tải server.

---

## 🏃 Khởi Chạy API Server

Chạy server ở chế độ phát triển (tự động reload khi sửa code):
```bash
npm run dev
```
Server sẽ chạy mặc định tại: `http://localhost:3000`.
