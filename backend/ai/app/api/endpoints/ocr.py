from fastapi import APIRouter, UploadFile, File, HTTPException
from app.services.ocr_service import process_prescription
import logging

router = APIRouter()
logger = logging.getLogger(__name__)

@router.post("/ocr")
async def ocr_endpoint(file: UploadFile = File(...)):
    """
    Endpoint to upload a prescription image, extract text via PaddleOCR,
    and parse the medicines using local LLM.
    """
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image.")
    
    try:
        # Read file bytes
        image_bytes = await file.read()
        
        # Invoke LangGraph pipeline
        result_state = process_prescription(image_bytes)
        
        return {
            "success": True,
            "data": result_state.get("structured_data"),
            "raw_text": result_state.get("raw_ocr_text")
        }
    except Exception as e:
        logger.error(f"Error processing OCR upload: {e}")
        raise HTTPException(status_code=500, detail=str(e))
