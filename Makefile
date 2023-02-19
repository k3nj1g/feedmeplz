POSTGRES_USER=chief
POSTGRES_PASSWORD=vS2!toGg
POSTGRES_PORT=5433
POSTGRES_DB=feedme

.EXPORT_ALL_VARIABLES:

deps:
	curl https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/65727574dfcd264acbb0c3e07860e4e9e9b22185/rus.traineddata -o rus.traineddata

macos:
	brew install pyenv pylint curl tesseract

up:
	cd back && docker-compose up -d

down:
	cd back && docker-compose down
