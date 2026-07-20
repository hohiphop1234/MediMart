from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import StreamingResponse
import httpx
import logging

from app.schemas.ocr import ChatCompletionRequest
from app.api.deps import get_http_client
from app.core.config import LLAMA_API_URL

router = APIRouter()
logger = logging.getLogger(__name__)

@router.post("/chat")
async def chat_endpoint(request: ChatCompletionRequest, client: httpx.AsyncClient = Depends(get_http_client)):
    body = request.model_dump(exclude_none=True)
    is_stream = body.get("stream", False)
    
    try:
        if is_stream:
            async def stream_generator():
                try:
                    async with client.stream("POST", LLAMA_API_URL, json=body) as response:
                        response.raise_for_status()
                        async for chunk in response.aiter_bytes():
                            yield chunk
                except httpx.HTTPError as exc:
                    logger.error(f"Error during streaming from llama-server: {exc}")
            return StreamingResponse(stream_generator(), media_type="text/event-stream")
        else:
            response = await client.post(LLAMA_API_URL, json=body)
            response.raise_for_status()
            return response.json()
    except httpx.HTTPStatusError as exc:
        raise HTTPException(status_code=exc.response.status_code, detail=f"Llama-server returned error: {exc.response.text}")
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=502, detail=f"Bad Gateway: Unable to connect to llama-server. {exc}")
