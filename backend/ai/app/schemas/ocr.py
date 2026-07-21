from pydantic import BaseModel, Field
from typing import List, Optional

# --- OCR Models ---

class ProductExtraction(BaseModel):
    product_name: str = Field(description="Name of the medicine or product")

class PrescriptionData(BaseModel):
    medicines: List[ProductExtraction] = Field(description="List of extracted medicines")
    notes: Optional[str] = Field(default=None, description="Any additional notes on the prescription")

# --- Chat Models ---

class ChatMessage(BaseModel):
    role: str = Field(..., description="Role of the message author (e.g., 'system', 'user', 'assistant')")
    content: str = Field(..., description="Content of the message")

class ChatCompletionRequest(BaseModel):
    messages: List[ChatMessage] = Field(..., description="List of messages comprising the conversation history")
    stream: Optional[bool] = Field(default=False, description="Whether to stream the response chunks")
    temperature: Optional[float] = Field(default=None, description="Sampling temperature")
    max_tokens: Optional[int] = Field(default=None, description="Maximum number of tokens to generate")
    
    model_config = {
        "extra": "allow",
        "json_schema_extra": {
            "example": {
                "messages": [
                    {"role": "user", "content": "Hello! Introduce yourself."}
                ],
                "stream": False
            }
        }
    }
