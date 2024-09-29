POSTGRES_USER=chef
POSTGRES_PASSWORD=vS2!toGg
POSTGRES_PORT=5433
POSTGRES_DB=feedme

.EXPORT_ALL_VARIABLES:

up:
	docker-compose up -d

down:
	docker-compose down

repl:
	clj -M:dev:repl/reloaded
