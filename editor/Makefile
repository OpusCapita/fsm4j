.DEFAULT_GOAL := help
.PHONY: all clean install uninstall

.PHONY: install-dependencies
install-dependencies:
	npm i

.PHONY: clean
clean:
	rm -rf node_modules
	rm -rf lib

.PHONY: test
test: install-dependencies
	npm run test

.PHONY: deploy
deploy:
	npm publish

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
