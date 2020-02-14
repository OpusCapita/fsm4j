.DEFAULT_GOAL := help

# goals which need credentials for NPM
include $(shell ./build/grails/configure-maven.sh)
include $(shell ./build/node/configure-npm.sh)

#-----------
# core tasks
#-----------
.PHONY: clean-core
clean-core:
	$(MAKE) -C core clean

.PHONY: test-core
test-core:
	$(MAKE) -C core test

.PHONY: install-core
install-core:
	$(MAKE) -C core install

.PHONY: deploy-core
deploy-core:
	$(MAKE) -C core deploy

# -------------
# history tasks
#--------------
.PHONY: clean-history
clean-history:
	$(MAKE) -C history clean

.PHONY: test-history
test-history:
	$(MAKE) -C history test

.PHONY: install-history
install-history:
	$(MAKE) -C history install

.PHONY: deploy-history
deploy-history:
	$(MAKE) -C history deploy

#-------------
# editor tasks
#-------------
.PHONY: clean-editor
clean-editor:
	$(MAKE) -C editor clean

.PHONY: test-editor
test-editor:
	$(MAKE) -C editor test

.PHONY: build-editor
build-editor:
	$(MAKE) -C editor build

#-----------
# demo tasks
#-----------
.PHONY: clean-demo
clean-demo:
	$(MAKE) -C demo clean

.PHONY: install-demo
install-demo:
	$(MAKE) -C demo install

.PHONY: start-demo
start-demo:
	$(MAKE) -C demo start

build-demo:
	$(MAKE) -C demo build

#---------------
# groupped tasks
#---------------
.PHONY: clean
clean: clean-core clean-history clean-editor clean-demo

.PHONY: start
start: install-core install-history build-editor start-demo

.PHONY: build
build: install-core install-history build-editor build-demo

.PHONY: deploy
deploy: deploy-core deploy-history build-editor deploy-demo

.PHONY: test
test: test-core test-history test-editor

#-------------
# docker tasks
#-------------
.PHONY: docker-auth
docker-auth: ## Login to Dockerhub
	./build/docker/docker-auth.sh

.PHONY: build-docker-ci
build-docker-ci: docker-auth ## Build CI Docker image
	./build/docker/ci/build.sh

.PHONY: publish-docker-ci
publish-docker-ci: docker-auth ## Publish CI Docker image
	./build/docker/ci/push.sh

.PHONY: build-docker
build-docker: docker-auth build ## Build application Docker image
	./build/docker/application/build.sh

.PHONY: publish-docker
publish-docker: docker-auth ## Publish application Docker image
	./build/docker/application/push.sh

#------------
# asure tasks
#------------
.PHONY: deploy-demo
deploy-demo: ## Deploy demo to cloud
	./build/demo/deploy.sh --ci-build-url=$(CI_BUILD_URL)

.PHONY: destroy-demo
destroy-demo: ## Destroy demo in a cloud
	./build/demo/destroy.sh

.PHONY: help
help:
	@awk '{ if (NF == 2 && $$1 == "include") { while ((getline line < $$2) > 0) print line ; close($$2) } else print }' $(firstword $(MAKEFILE_LIST)) \
		| grep -E '^[a-zA-Z_-]+:.*?## .*$$' \
		| sort \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'