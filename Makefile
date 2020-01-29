.DEFAULT_GOAL := help
.PHONY: all clean install uninstall


# goals which need credentials for NPM
include $(shell ./build/grails/configure-maven.sh)
include $(shell ./build/node/configure-npm.sh)

#-----------
# core tasks
#-----------
.PHONY: clean-core
clean-core:
	cd core && mvn clean

.PHONY: test-core
test-core:
	cd core && mvn test

# -------------
# history tasks
#--------------
.PHONY: clean-history
clean-history:
	cd history && mvn clean

.PHONY: test-history
test-history:
	cd history && grails test-app

#------------------------
# integration tests tasks
#------------------------
.PHONY: clean-integration-tests
clean-integration-tests:
	cd integration-tests && grails clean

.PHONY: test-integration-tests
test-integration-tests:
	cd core && mvn -Dmaven.test.skip install
	cd integration-tests && grails test-app

#-------------
# editor tasks
#-------------
.PHONY: clean-editor
clean-editor:
	cd editor && rm -rf node_modules

.PHONY: test-editor
test-editor:
	cd editor && npm install && npm test

#clean all
.PHONY: clean
clean: clean-core clean-history clean-integration-tests clean-editor
#test all
.PHONY: test
test: test-core test-history test-integration-tests test-editor

#-----------
# demo goals
#-----------
.PHONY: clean-demo
clean-demo:
	cd demo/client && rm -rf node_modules
	cd editor && rm -rf node_modules && rm -rf lib

.PHONY: build-client-demo
build-client-demo: clean-demo
	cd demo/client && npm install
	cd editor && npm install && npm publish --tag latest --dry-run
	cd demo/client && npm run demo:build

.PHONY: build-client-demo-watch
build-client-demo-watch: clean-demo
	cd demo/client && npm install
	cd editor && npm install && npm link ../demo/client
	cd demo/client && npm run demo:build-watch

.PHONY: start-server-demo
start-server-demo:
	cd core && mvn -Dmaven.test.skip install
	cd demo/server && grails run-app

# running server with
.PHONY: start
start:
	$(MAKE) -j build-client-demo-watch start-server-demo
	#$(MAKE) --output-sync=recurse -j build-client-demo-watch start-server-demo

.PHONY: build-client-demo
build: build-client-demo
	cd demo/server && grails war

.PHONY: deploy
deploy: test build-client-demo
	cd core && mvn -Dmaven.test.skip deploy
	cd history && grails maven-deploy -Dgrails.env=prod -verbose
	cd editor && npm run publish-release
	cd demo/server && grails maven-deploy -Dgrails.env=prod -verbose

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

.PHONY: deploy-demo
deploy-demo: ## Deploy demo to cloud
	./build/demo/deploy.sh --ci-build-url=$(CI_BUILD_URL)

.PHONY: destroy-demo
destroy-demo: ## Destroy demo in a cloud
	./build/demo/destroy.sh

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' && echo "\nNOTE: You can find Makefile goals implementation stored in \"./build\" directory"
