import logging
import os
# Disable PaddlePaddle PIR (new IR) to prevent OneDNN double attribute crash
os.environ["FLAGS_enable_pir_api"] = "0"
os.environ["FLAGS_use_mkldnn"] = "1"
os.environ["PADDLE_PDX_ENABLE_MKLDNN_BYDEFAULT"] = "1"

from typing import TypedDict, Any
from paddleocr import PaddleOCR
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langgraph.graph import StateGraph, START, END

from app.schemas.ocr import PrescriptionData
from app.utils.image_processing import process_prescription_image
from app.core.config import LLAMA_API_URL
from app.prompts import OCR_SYSTEM_PROMPT, OCR_HUMAN_PROMPT

logger = logging.getLogger(__name__)

# Initialize PaddleOCR globally so it's loaded only once
ocr = PaddleOCR(use_textline_orientation=True, lang="vi", enable_mkldnn=True)

# Define LangGraph State
class OCRState(TypedDict):
    image_bytes: bytes
    raw_ocr_text: str
    structured_data: dict

def ocr_extraction_node(state: OCRState) -> dict:
    """Node to process image and run PaddleOCR."""
    logger.info("Starting image preprocessing and OCR...")
    # Preprocess
    cv_img = process_prescription_image(state["image_bytes"])
    
    # Run OCR
    # paddleocr expects numpy array (BGR)
    result_generator = ocr.predict(cv_img)
    result = list(result_generator)
    
    # Extract text from result
    extracted_text = []
    if result and isinstance(result[0], dict):
        extracted_text = result[0].get("rec_texts", [])
    elif result and isinstance(result[0], list) and result[0]:
        for line in result[0]:
            # line is [ [box], (text, confidence) ]
            if len(line) > 1 and isinstance(line[1], (tuple, list)):
                text = line[1][0]
                extracted_text.append(text)
            
    raw_text = "\n".join(extracted_text)
    logger.info("OCR Extraction completed.")
    return {"raw_ocr_text": raw_text}

def llm_parsing_node(state: OCRState) -> dict:
    """Node to parse OCR text using LLM."""
    logger.info("Starting LLM parsing of OCR text...")
    
    # Configure local LLM via OpenAI API format
    base_url = LLAMA_API_URL.replace("/chat/completions", "")
    
    llm = ChatOpenAI(
        model="local-model",
        base_url=base_url,
        api_key="not-needed",
        temperature=0.0,
        max_tokens=1000,
        stop=["<|im_end|>", "</s>"]
    )
    
    # Create prompt
    prompt = ChatPromptTemplate.from_messages([
        ("system", OCR_SYSTEM_PROMPT),
        ("human", OCR_HUMAN_PROMPT)
    ])
    
    chain = prompt | llm
    
    try:
        response = chain.invoke({"ocr_text": state["raw_ocr_text"]})
        content = response.content.strip()
        logger.info(f"LLM raw response: {content}")
        
        # Clean markdown code block wraps if LLM still added them
        if content.startswith("```json"):
            content = content[7:]
        elif content.startswith("```"):
            content = content[3:]
        if content.endswith("```"):
            content = content[:-3]
        content = content.strip()
        
        # Parse JSON with auto-repair for missing closing brackets/braces
        import json
        try:
            data = json.loads(content)
        except json.JSONDecodeError:
            # Count open vs close braces/brackets and append missing ones
            open_braces = content.count("{")
            close_braces = content.count("}")
            open_brackets = content.count("[")
            close_brackets = content.count("]")
            
            repaired_content = content
            if open_brackets > close_brackets:
                repaired_content += "]" * (open_brackets - close_brackets)
            if open_braces > close_braces:
                repaired_content += "}" * (open_braces - close_braces)
                
            logger.info(f"Attempting to parse repaired JSON: {repaired_content}")
            data = json.loads(repaired_content)
        
        # Validate data matches PrescriptionData schema structure
        validated_medicines = []
        for med in data.get("medicines", []):
            if "product_name" in med and "quantity" in med:
                validated_medicines.append({
                    "product_name": str(med["product_name"]),
                    "quantity": str(med["quantity"])
                })
        
        structured_data = {
            "medicines": validated_medicines,
            "notes": str(data.get("notes", ""))
        }
        return {"structured_data": structured_data}
    except Exception as e:
        logger.error(f"Error parsing with LLM: {e}. Raw content: {content if 'content' in locals() else 'None'}")
        return {"structured_data": {"medicines": [], "notes": f"LLM parsing failed: {str(e)}" }}

# Build LangGraph
graph_builder = StateGraph(OCRState)
graph_builder.add_node("extract_ocr", ocr_extraction_node)
graph_builder.add_node("parse_llm", llm_parsing_node)

graph_builder.add_edge(START, "extract_ocr")
graph_builder.add_edge("extract_ocr", "parse_llm")
graph_builder.add_edge("parse_llm", END)

ocr_graph = graph_builder.compile()

def process_prescription(image_bytes: bytes) -> dict:
    """Main entry point to execute the graph."""
    final_state = ocr_graph.invoke({"image_bytes": image_bytes})
    return final_state
