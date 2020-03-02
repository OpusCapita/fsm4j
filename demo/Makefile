.DEFAULT_GOAL := help

.PHONY: clean
clean: ## Clean
	$(MAKE) -C client clean
	$(MAKE) -C server clean

.PHONY: deploy
deploy: ## Deploy
	$(MAKE) -C client build
	$(MAKE) -C server deploy

.PHONY: start-client
start-client: ## Start client
	$(MAKE) -C client build-watch

.PHONY: start-server
start-server: ## Start server
	$(MAKE) -C server start

.PHONY: start
start: ## Start
	$(MAKE) -j start-client start-server

build: ## Build
	$(MAKE) -C client build
	$(MAKE) -C server build

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
