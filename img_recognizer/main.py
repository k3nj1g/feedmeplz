import os
import re
import cv2
import pytesseract

image_name = "test.jpg"
os.environ["TESSDATA_PREFIX"] = "."
split_words = ["Салаты", "Суп", "Второе", "Гарниры"]

# Read image
try:
    image = cv2.imread(image_name)
    print("Image opened successfully\n")
    # Pre-processing
    gray_text = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    # Recognize text using Tesseract
    raw_text = pytesseract.image_to_string(gray_text, lang="rus", config="--psm 11")
    raw_text = raw_text.replace('.', '')
    raw_text = raw_text.replace('\n\n', ' ')
    text = re.split("Салаты|Суп|Второе|Гарниры", raw_text)
    menu = dict()
    for i in split_words:
        menu[i] = text[split_words.index(i) + 1]
        menu[i] = menu[i].strip()
    for i in menu:
        menu[i] = re.split("руб|Руб", menu[i])
        menu[i].pop(-1) # удаляет пустой последний элемент
    print(menu["Суп"])

except Exception as e:
    print(f"Error: {e}")
    print(f"No image found: {image_name}")
