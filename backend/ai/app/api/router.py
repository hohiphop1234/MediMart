from fastapi import APIRouter
from app.api.endpoints import chat, ocr

api_router = APIRouter()
api_router.include_router(chat.router, tags=["chat"])
api_router.include_router(ocr.router, tags=["ocr"])

