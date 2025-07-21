.PHONY: dev

ifneq (,$(wildcard .env))
    include .env
    export
endif

dev:
	npm run dev

dev-https:
	./scripts/dev.sh
