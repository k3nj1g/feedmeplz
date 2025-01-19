.PHONY: dev

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
