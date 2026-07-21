# MediMart AI Backend

Đây là dịch vụ xử lý Trí tuệ Nhân tạo (AI Backend) cho dự án **MediMart**, được xây dựng trên nền tảng **FastAPI**, tích hợp công nghệ trích xuất văn bản hình ảnh **PaddleOCR (PP-OCRv6)** và Mô hình Ngôn ngữ Lớn cục bộ **Qwen 3** chạy qua **llama-server**.

Dịch vụ này tự động chạy một mô hình LLM chuyên biệt về y khoa cục bộ trên máy và cung cấp các API xử lý phân tích đơn thuốc tự động từ ảnh chụp.

---

## 🛠️ Công Nghệ Sử Dụng

1. **FastAPI**: Cung cấp Web API hiệu năng cao chạy ở cổng `8000`.
2. **PaddleOCR 3.7.0 (PP-OCRv6)**: Hệ thống nhận diện ký tự quang học (OCR) thế hệ mới nhất cho tiếng Việt (`lang="vi"`), hỗ trợ tăng tốc phần cứng thông qua thư viện **oneDNN (MKLDNN)** trên CPU.
3. **Llama-Server**: Subprocess chạy ngầm ở cổng `8081` để tải và suy luận mô hình GGUF thông qua card đồ họa tích hợp hoặc rời (tối ưu hóa Vulkan cho AMD 780m hoặc các GPU tương tự).
4. **LangGraph**: Điều phối luồng xử lý (Pipeline) của OCR:
   * Ảnh tải lên ➜ Tiền xử lý (Xoay ảnh EXIF, Resize) ➜ Trích xuất chữ viết thô (PaddleOCRv6) ➜ LLM phân tích cấu trúc ➜ Kết quả JSON có cấu trúc (danh sách thuốc, liều dùng, ghi chú).

---

## 📂 Cấu Trúc Thư Mục Chính

```text
backend/ai/
├── app/
│   ├── api/
│   │   ├── endpoints/       # Các file định nghĩa endpoint API (/chat, /ocr)
│   │   ├── deps.py          # Quản lý HTTP client dùng chung
│   │   └── router.py        # Định tuyến các API
│   ├── core/
│   │   ├── config.py        # Cấu hình đường dẫn mô hình, llama-server và cổng kết nối
│   │   └── llama_manager.py # Bộ quản lý tiến trình chạy ngầm llama-server (Singleton)
│   ├── schemas/
│   │   └── ocr.py           # Pydantic schemas cho dữ liệu đầu vào/đầu ra
│   ├── services/
│   │   └── ocr_service.py   # Định nghĩa pipeline LangGraph cho luồng OCR & LLM
│   ├── utils/
│   │   └── image_processing.py # Các hàm tiền xử lý ảnh (xoay, cắt, chỉnh kích thước)
│   └── main.py              # File khởi chạy ứng dụng FastAPI chính
├── llama-b10069-bin-win-vulkan-x64/ # Thư mục chứa tệp thực thi llama-server.exe
├── models/                  # Nơi đặt tệp trọng số GGUF (qwen3-4b.medical-consultant.Q4_K_M.gguf)
├── pyproject.toml           # Định nghĩa dependencies (sử dụng uv làm quản lý package)
├── main.py                  # Entrypoint chính của backend
└── test_ocr.py              # File script kiểm thử gửi request OCR
```

---

## 🚀 Hướng Dẫn Cài Đặt

### 1. Yêu Cầu Hệ Thống
* **Python**: Phiên bản `>=3.11`
* **UV**: Quản lý package nhanh và tối ưu (Khuyến nghị sử dụng). Tải UV bằng lệnh:
  ```powershell
  powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
  ```
* **Hardware**: CPU hỗ trợ tập lệnh tối ưu toán học (oneDNN) và GPU tích hợp/rời hỗ trợ Vulkan để tăng tốc độ xử lý LLM.

### 2. Cài Đặt Dependencies
Di chuyển vào thư mục `/backend/ai` và chạy lệnh sau để đồng bộ hóa môi trường ảo và cài đặt tất cả thư viện:
```bash
uv sync
```
*Lưu ý: Môi trường ảo `.venv` sẽ tự động được tạo và cấu hình.*

> [!IMPORTANT]
> **Cấu hình CPU Acceleration (oneDNN/MKLDNN)**
> Dự án sử dụng phiên bản `paddlepaddle==3.2.2` để tránh lỗi crash tương thích (regression bug) của bản 3.3.x khi chạy trên CPU. Điều này giúp bật thành công tính năng tăng tốc CPU **MKLDNN** trên cả chip Intel và AMD, mang lại hiệu năng OCR vượt trội.

---

## 🏃 Khởi Chạy Backend

Để khởi chạy server backend AI, chạy lệnh sau tại thư mục `/backend/ai`:

```bash
.venv\Scripts\python main.py
```

Tiến trình khởi chạy sẽ thực hiện:
1. Tạo tiến trình chạy ngầm `llama-server.exe` tải mô hình ngôn ngữ y khoa tại cổng `8081` (Tự động tắt khi FastAPI tắt thông qua `atexit`).
2. Khởi tạo đối tượng `PaddleOCR` và tải các mô hình PP-OCRv6 Medium lên bộ nhớ cache.
3. Chạy FastAPI ứng dụng chính tại địa chỉ: `http://localhost:8000`.

---

## 🔌 API Endpoints

### 1. Chat Completions (`POST /api/chat`)
Proxy trực tiếp các yêu cầu chat đến mô hình LLM y tế chạy cục bộ, tương thích định dạng API của OpenAI (hỗ trợ streaming).

* **URL**: `/api/chat`
* **Body**:
  ```json
  {
    "messages": [
      {"role": "user", "content": "Tên thuốc Paracetamol dùng để làm gì?"}
    ],
    "stream": false
  }
  ```

### 2. Đọc và Phân Tích Đơn Thuốc (`POST /api/ocr`)
Nhận dạng chữ viết tiếng Việt trên hình ảnh đơn thuốc và chuyển đổi thành thông tin số hóa có cấu trúc.

* **URL**: `/api/ocr`
* **Method**: `POST`
* **Request Type**: `multipart/form-data`
* **Parameters**:
  * `file`: Tệp tin hình ảnh đơn thuốc (JPEG/PNG).
* **Response**:
  ```json
  {
    "success": true,
    "data": {
      "medicines": [
        {
          "product_name": "Ciprofloxacin",
          "quantity": "03 viên"
        },
        {
          "product_name": "Lactobacillus",
          "quantity": "20 gói"
        }
      ],
      "notes": "Công khoản: 4 khoản"
    },
    "raw_text": "Thông tin chữ viết thô được nhận diện bởi OCR..."
  }
  ```

---

## 🧪 Kiểm Thử

### 1. Kiểm thử trích xuất đơn thuốc (OCR)
Bạn có thể chạy tệp script kiểm thử gửi request ảnh đơn thuốc mẫu có sẵn trong thư mục `examples` để kiểm nghiệm tính năng:

```bash
.venv\Scripts\python test_ocr.py
```

### 2. Kiểm thử hội thoại tư vấn sức khỏe (Chatbot)
Để kiểm thử khả năng hội thoại trực tiếp với chatbot y tế (hỗ trợ tư vấn sức khỏe, hỏi đáp bệnh lý và thuốc khuyên dùng), chạy tệp script:

```bash
.venv\Scripts\python cli.py
```

*Lưu ý: Đảm bảo server backend đang chạy (tại cổng 8000) trước khi thực hiện kiểm thử.*

