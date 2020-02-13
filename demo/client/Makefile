.DEFAULT_GOAL := help
.PHONY: all clean install uninstall

.PHONY: install-dependencies
install-dependencies:
	npm i
	npm link ../../editor

.PHONY: clean
clean:
	rm -rf node_modules

.PHONY: deploy
build: install-dependencies
	npm run demo:build

build-watch: install-dependencies
	npm run demo:build-watch

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
