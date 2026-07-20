import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

LLAMA_SERVER_EXE = os.path.join(BASE_DIR, "llama-b9850-bin-win-vulkan-x64", "llama-server.exe")
MODEL_PATH = os.path.join(BASE_DIR, "models", "qwen3-4b.medical-consultant.Q4_K_M.gguf")

LLAMA_HOST = "127.0.0.1"
LLAMA_PORT = 8081
LLAMA_API_URL = f"http://{LLAMA_HOST}:{LLAMA_PORT}/v1/chat/completions"
