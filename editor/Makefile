.DEFAULT_GOAL := help

.PHONY: install-dependencies
install-dependencies:
	npm i

.PHONY: clean
clean:
	rm -rf node_modules
	rm -rf lib
	rm -rf .nyc_output
	rm -rf coverage

.PHONY: test
test: install-dependencies ## Run tests
	npm run test

.PHONY: build
build: install-dependencies ## Build
	npm run build

.PHONY: publish
deploy: install-dependencies ## Deploy
	npm publish --dry-run

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
