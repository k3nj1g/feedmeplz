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

build:
	clj -T:build release

deploy-up:
	docker compose -f ../docker-compose.deploy.yaml up -d

deploy-down:
	docker compose -f ../docker-compose.deploy.yaml down

psql:
	docker exec -it postgresdb-db-1 psql -U $(POSTGRES_USER) $(POSTGRES_DB)
