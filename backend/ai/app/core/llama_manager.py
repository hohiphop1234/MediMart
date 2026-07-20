import os
import subprocess
import asyncio
import httpx
import atexit
import psutil

class LlamaManager:
    _instance = None
    
    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super(LlamaManager, cls).__new__(cls)
            cls._instance.process = None
        return cls._instance

    def __init__(self, model_path: str, server_exe: str, host: str = "127.0.0.1", port: int = 8081):
        if hasattr(self, '_initialized') and self._initialized:
            return
        self.model_path = model_path
        self.server_exe = server_exe
        self.host = host
        self.port = port
        self.process = None
        self.log_file = None
        self._initialized = True
        atexit.register(self.stop)

    async def start(self):
        if self.process is not None:
            print("llama-server is already running.")
            return

        threads = psutil.cpu_count(logical=False) or max(1, (os.cpu_count() or 8) // 2)

        print(f"Starting llama-server with model {self.model_path} on port {self.port} (Threads: {threads})...")
        cmd = [
            self.server_exe,
            "-m", self.model_path,
            "--host", self.host,
            "--port", str(self.port),
            "-ngl", "99",  # Offload layers to GPU for AMD 780m Vulkan
            "-fa", "on",   # Enable Flash Attention
            "-t", str(threads),
            "-c", "8192",  # Context size (increased to support thinking tokens)
            "--jinja",
            "--reasoning", "off"
        ]
        
        log_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "llama_log.txt")
        self.log_file = open(log_path, "w")
        self.process = subprocess.Popen(
            cmd,
            stdout=self.log_file,
            stderr=subprocess.STDOUT,
            cwd=os.path.dirname(self.server_exe)
        )
        
        # Wait for the server to be fully loaded and ready
        async with httpx.AsyncClient() as client:
            max_retries = 120
            for _ in range(max_retries):
                try:
                    res = await client.get(f"http://{self.host}:{self.port}/health", timeout=1.0)
                    if res.status_code == 200:
                        data = res.json()
                        if data.get("status") == "ok":
                            print("llama-server is fully loaded and ready to accept requests.")
                            return
                except (httpx.RequestError, ValueError):
                    pass
                await asyncio.sleep(1)
            print("Warning: llama-server health check timed out. It might not be ready.")

    def stop(self):
        if self.process:
            print("Shutting down llama-server...")
            self.process.terminate()
            try:
                self.process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self.process.kill()
            self.process = None
        if self.log_file and not self.log_file.closed:
            self.log_file.close()
