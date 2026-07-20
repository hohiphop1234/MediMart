#!/usr/bin/env python3
import sys
import json
import httpx
from app.prompts import CHAT_SYSTEM_PROMPT

# Reconfigure stdout and stdin to use UTF-8 to prevent encoding issues on Windows consoles
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if hasattr(sys.stdin, 'reconfigure'):
    sys.stdin.reconfigure(encoding='utf-8')

# API URL của chat backend
API_URL = "http://localhost:8000/api/chat"

# ANSI color codes để giao diện CLI chuyên nghiệp hơn
BLUE = "\033[94m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
CYAN = "\033[96m"
RESET = "\033[0m"
BOLD = "\033[1m"

def print_header():
    print(f"{BLUE}{BOLD}========================================================={RESET}")
    print(f"{BLUE}{BOLD}     MediMart Chatbot - Giao Diện Kiểm Thử Hội Thoại     {RESET}")
    print(f"{BLUE}{BOLD}========================================================={RESET}")
    print(f"{YELLOW}* Chatbot tư vấn sức khỏe, hỏi đáp bệnh lý và thuốc sử dụng.{RESET}")
    print(f"{YELLOW}* Các lệnh đặc biệt:{RESET}")
    print(f"  - {BOLD}clear{RESET} / {BOLD}reset{RESET}: Xóa lịch sử hội thoại.")
    print(f"  - {BOLD}exit{RESET} / {BOLD}quit{RESET}: Thoát chương trình.")
    print(f"{BLUE}{BOLD}========================================================={RESET}\n")

def chat_loop():
    # Prompt hệ thống ngắn gọn, tối ưu để mô hình Qwen 4B phản hồi trực tiếp, tránh lặp lại giới thiệu
    system_prompt = CHAT_SYSTEM_PROMPT
    
    messages = [
        {"role": "system", "content": system_prompt}
    ]
    
    print_header()
    
    while True:
        try:
            # Nhận tin nhắn từ người dùng
            user_input = input(f"{GREEN}{BOLD}Bạn: {RESET}").strip()
            
            if not user_input:
                continue
                
            # Xử lý các lệnh đặc biệt
            if user_input.lower() in ["exit", "quit"]:
                print(f"\n{RED}Tạm biệt! Chúc bạn luôn khỏe mạnh.{RESET}\n")
                break
                
            if user_input.lower() in ["clear", "reset"]:
                messages = [{"role": "system", "content": system_prompt}]
                print(f"\n{YELLOW}[Hệ thống] Đã làm mới lịch sử trò chuyện.{RESET}\n")
                continue
                
            # Lưu tin nhắn người dùng vào lịch sử hội thoại
            messages.append({"role": "user", "content": user_input})
            
            # Cấu hình payload request
            payload = {
                "messages": messages,
                "stream": True,
                "temperature": 0.7,
                "max_tokens": 1024
            }
            
            print(f"\n{CYAN}{BOLD}MediMart AI: {RESET}", end="", flush=True)
            
            full_response = ""
            
            # Gửi request stream đến API
            with httpx.stream("POST", API_URL, json=payload, timeout=90.0) as r:
                if r.status_code != 200:
                    # Đọc nội dung lỗi khi HTTP status không phải 200
                    error_msg = r.read().decode("utf-8", errors="ignore")
                    print(f"\n{RED}Lỗi API ({r.status_code}): {error_msg}{RESET}")
                    # Xóa tin nhắn cuối của user vì request thất bại
                    messages.pop()
                    continue
                
                for line in r.iter_lines():
                    if line.startswith("data: "):
                        data_str = line[6:].strip() # Bỏ phần tiền tố "data: "
                        if data_str == "[DONE]":
                            break
                        try:
                            data_json = json.loads(data_str)
                            choices = data_json.get("choices", [])
                            if choices:
                                delta = choices[0].get("delta", {})
                                content = delta.get("content", "")
                                if content:
                                    print(content, end="", flush=True)
                                    full_response += content
                        except json.JSONDecodeError:
                            pass
            
            print("\n") # Xuống dòng khi kết thúc stream
            
            # Lưu câu trả lời của AI vào lịch sử hội thoại
            if full_response:
                messages.append({"role": "assistant", "content": full_response})
                
        except KeyboardInterrupt:
            print(f"\n\n{RED}Đã ngắt kết nối hội thoại.{RESET}\n")
            break
        except httpx.ConnectError:
            print(f"\n{RED}Lỗi kết nối: Không thể kết nối tới server tại {API_URL}.{RESET}")
            print(f"{YELLOW}Vui lòng chạy server bằng lệnh: .venv\\Scripts\\python main.py{RESET}\n")
            if messages and messages[-1]["role"] == "user":
                messages.pop()
        except Exception as e:
            print(f"\n{RED}Đã xảy ra lỗi hệ thống: {e}{RESET}\n")
            if messages and messages[-1]["role"] == "user":
                messages.pop()

if __name__ == "__main__":
    chat_loop()
