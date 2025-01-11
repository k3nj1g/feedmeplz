ifneq (,$(wildcard .env))
    include .env
    export
endif

up:
	docker-compose up -d

down:
	docker-compose down

repl:
	clj -M:dev:repl/reloaded
