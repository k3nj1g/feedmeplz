FROM python:3.11.1-alpine3.17
WORKDIR /app
RUN apk add --no-cache tesseract-ocr=5.2.0-r1
ADD https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/65727574dfcd264acbb0c3e07860e4e9e9b22185/rus.traineddata .
COPY requirements.txt .
RUN pip install -r requirements.txt
