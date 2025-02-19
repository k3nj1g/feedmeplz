.PHONY: dev psql

ifneq (,$(wildcard .env))
    include .env
    export
endif

up:
	docker-compose up -d

down:
	docker-compose down

dev:
	clj -M:dev:repl/reloaded

psql:
	docker-compose exec -e PGPASSWORD=${POSTGRES_PASSWORD} db psql -h db -U ${POSTGRES_USER} -d ${POSTGRES_DB}
