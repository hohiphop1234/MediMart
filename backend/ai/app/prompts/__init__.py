import os

PROMPTS_DIR = os.path.dirname(os.path.abspath(__file__))

def load_prompt(filename: str) -> str:
    path = os.path.join(PROMPTS_DIR, filename)
    with open(path, "r", encoding="utf-8") as f:
        return f.read().strip()

OCR_SYSTEM_PROMPT = load_prompt("ocr_system.txt")
OCR_HUMAN_PROMPT = load_prompt("ocr_human.txt")
CHAT_SYSTEM_PROMPT = load_prompt("chat_system.txt")

__all__ = [
    "OCR_SYSTEM_PROMPT",
    "OCR_HUMAN_PROMPT",
    "CHAT_SYSTEM_PROMPT",
]
