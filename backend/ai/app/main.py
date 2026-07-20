from fastapi import FastAPI
from contextlib import asynccontextmanager
import httpx
import uvicorn
import asyncio
import logging
import sys
import os

# Ensure the root directory is in sys.path so 'app' can be imported when running directly
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.api.router import api_router
from app.core.llama_manager import LlamaManager
from app.core.config import MODEL_PATH, LLAMA_SERVER_EXE, LLAMA_HOST, LLAMA_PORT

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize llama manager (singleton)
manager = LlamaManager(
    model_path=MODEL_PATH,
    server_exe=LLAMA_SERVER_EXE,
    host=LLAMA_HOST,
    port=LLAMA_PORT
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting up resources...")
    # Start the llama-server subprocess asynchronously
    asyncio.create_task(manager.start())
    
    # Initialize a shared httpx client to proxy requests to llama-server
    # using a shared client helps avoid connection overhead
    app.state.client = httpx.AsyncClient(timeout=120.0)
    
    yield
    
    logger.info("Shutting down resources...")
    # Close httpx client
    await app.state.client.aclose()
    
    # Shutdown llama-server subprocess gracefully
    manager.stop()

app = FastAPI(
    title="MediMart AI Backend",
    version="1.0.0",
    lifespan=lifespan
)

# Include the API router
app.include_router(api_router, prefix="/api")

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
