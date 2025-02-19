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
	docker exec -it postgresdb-db-1 psql -U $(POSTGRES_USER) $(POSTGRES_DB)
