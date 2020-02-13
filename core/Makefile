.DEFAULT_GOAL := help
.PHONY: all clean install uninstall

.PHONY: clean
clean:
	mvn clean

.PHONY: test
test:
	mvn verify

.PHONY: install
install:
	mvn install

.PHONY: deploy
deploy:
	mvn deploy

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
