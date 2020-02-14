.DEFAULT_GOAL := help

.PHONY: clean
clean:
	$(MAKE) -C client clean
	$(MAKE) -C server clean

.PHONY: deploy
deploy:
	$(MAKE) -C client build
	$(MAKE) -C server deploy

.PHONY: start-client
start-client:
	$(MAKE) -C client build-watch

.PHONY: start-server
start-server:
	$(MAKE) -C server start

.PHONY: start
start:
	$(MAKE) -j start-client start-server

build:
	$(MAKE) -C client build
	$(MAKE) -C server build

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
