.DEFAULT_GOAL := help

.PHONY: clean
clean:
	rm -rf target

.PHONY: refresh
refresh: clean
	grails refresh-dependencies

.PHONY: install
install: refresh
	grails maven-install --offline

.PHONY: deploy
deploy: refresh
	grails maven-deploy -Dgrails.env=prod -verbose --offline

start: clean refresh
	grails run-app --offline

build: clean refresh
	grails war -Dgrails.env=prod -verbose --offline

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
