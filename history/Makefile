.DEFAULT_GOAL := help

.PHONY: clean
clean:
	rm -rf target

.PHONY: test
test:
	grails test-app --stacktrace

.PHONY: refresh
refresh:
	grails refresh-dependencies

.PHONY: install
install: refresh
	grails maven-install --offline

.PHONY: deploy
deploy: refresh
	grails maven-deploy --offline

.PHONY: doc
doc: ## Build technical documentation
	../build/grails/doc.sh

.PHONY: doc-publish
doc-publish: ## Publish technical documentation
	../build/grails/doc-publish.sh

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
