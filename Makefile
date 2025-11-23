# Database configuration
POSTGRES_USER=chief
POSTGRES_PASSWORD=vS2!toGg
POSTGRES_PORT=5433
POSTGRES_DB=feedme

# Project directories
BACK_DIR=web_app/back
FRONT_DIR=web_app/front

.EXPORT_ALL_VARIABLES:
.PHONY: help up down clean logs back-dev

# Default target
help:
	@echo "Available commands:"
	@echo "  make up         - Start Docker containers"
	@echo "  make down       - Stop Docker containers"
	@echo "  make install    - Install all dependencies"
	@echo "  make logs       - Show Docker logs"
	@echo "  back-dev	     - Start backend development environment"
	@echo "  front-dev       - Start frontend development environment"
up:
	@echo "Starting Docker containers..."
	cd $(BACK_DIR) && docker compose up -d

down:
	@echo "Stopping Docker containers..."
	cd $(BACK_DIR) && docker compose down

logs:
	cd $(BACK_DIR) && docker compose logs -f

clean:
	cd $(BACK_DIR) && docker compose down -v
	@echo "Cleanup complete"

back-dev:
	cd $(BACK_DIR) && make dev

front-dev:
	cd $(FRONT_DIR) && make dev
