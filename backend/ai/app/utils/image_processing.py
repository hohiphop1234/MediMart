import cv2
import numpy as np
from PIL import Image, ImageOps
import io

def fix_orientation(image_bytes: bytes) -> Image.Image:
    """Fix image orientation based on EXIF data."""
    image = Image.open(io.BytesIO(image_bytes))
    image = ImageOps.exif_transpose(image)
    return image

def resize_image(image: Image.Image, max_dim: int = 2048) -> Image.Image:
    """Resize image to ensure the max dimension is within max_dim without losing aspect ratio."""
    width, height = image.size
    if width > max_dim or height > max_dim:
        image.thumbnail((max_dim, max_dim), Image.Resampling.LANCZOS)
    return image

def apply_clahe(cv_img: np.ndarray) -> np.ndarray:
    """Apply Contrast Limited Adaptive Histogram Equalization."""
    # Convert to LAB color space
    lab = cv2.cvtColor(cv_img, cv2.COLOR_BGR2LAB)
    l_channel, a, b = cv2.split(lab)
    
    # Apply CLAHE to L-channel
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    cl = clahe.apply(l_channel)
    
    # Merge the CLAHE enhanced L-channel with the a and b channel
    limg = cv2.merge((cl, a, b))
    
    # Convert image from LAB Color model to BGR color space
    enhanced_img = cv2.cvtColor(limg, cv2.COLOR_LAB2BGR)
    return enhanced_img

def order_points(pts):
    """Order points in top-left, top-right, bottom-right, bottom-left order."""
    rect = np.zeros((4, 2), dtype="float32")
    s = pts.sum(axis=1)
    rect[0] = pts[np.argmin(s)]
    rect[2] = pts[np.argmax(s)]
    diff = np.diff(pts, axis=1)
    rect[1] = pts[np.argmin(diff)]
    rect[3] = pts[np.argmax(diff)]
    return rect

def four_point_transform(image, pts):
    """Apply perspective transform to the image."""
    rect = order_points(pts)
    (tl, tr, br, bl) = rect

    # compute the width of the new image
    widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
    widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB))

    # compute the height of the new image
    heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
    heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB))

    dst = np.array([
        [0, 0],
        [maxWidth - 1, 0],
        [maxWidth - 1, maxHeight - 1],
        [0, maxHeight - 1]], dtype="float32")

    M = cv2.getPerspectiveTransform(rect, dst)
    warped = cv2.warpPerspective(image, M, (maxWidth, maxHeight))
    return warped

def apply_perspective_correction(cv_img: np.ndarray) -> np.ndarray:
    """Detect document edges and apply perspective transform if reliable."""
    gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    edged = cv2.Canny(blurred, 75, 200)

    # find contours in the edge map
    contours, _ = cv2.findContours(edged.copy(), cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)[:5]

    doc_cnt = None
    for c in contours:
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)

        # if our approximated contour has four points, we can assume it is the document
        if len(approx) == 4:
            area = cv2.contourArea(c)
            image_area = cv_img.shape[0] * cv_img.shape[1]
            
            # only correct if the detected rectangle is at least 20% of the image
            if area > 0.2 * image_area:
                doc_cnt = approx
                break

    if doc_cnt is not None:
        return four_point_transform(cv_img, doc_cnt.reshape(4, 2))
    return cv_img

def process_prescription_image(image_bytes: bytes) -> np.ndarray:
    """Full preprocessing pipeline for OCR."""
    # 1. EXIF fix
    pil_img = fix_orientation(image_bytes)
    
    # 2. Resize
    pil_img = resize_image(pil_img)
    
    # Convert PIL to CV2 (BGR)
    cv_img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
    
    # 3. Perspective correction
    cv_img = apply_perspective_correction(cv_img)
    
    # 4. CLAHE
    cv_img = apply_clahe(cv_img)
    
    return cv_img
