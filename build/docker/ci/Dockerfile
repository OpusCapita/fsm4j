FROM opuscapita/minsk-core-maven:3.3.9

# maintainer
LABEL maintainer="Alexey.Sergeev@opuscapita.com"

# installing
# - sdkman (https://sdkman.io/)
# And via sdkman installing
# - groovy
# - maven
# - grails
ENV SDKMAN_DIR /usr/local/sdkman
RUN curl -s https://get.sdkman.io | bash && \
    set -x && \
    echo "sdkman_auto_answer=true" > $SDKMAN_DIR/etc/config && \
    echo "sdkman_auto_selfupdate=false" >> $SDKMAN_DIR/etc/config && \
    echo "sdkman_insecure_ssl=false" >> $SDKMAN_DIR/etc/config && \
    /bin/bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && \
       sdk install groovy 2.4.13 && \
       sdk install grails 2.4.4"

# add groovy, grails to PATH
ENV PATH $SDKMAN_DIR/candidates/groovy/current/bin:$SDKMAN_DIR/candidates/grails/current/bin:$PATH

# install docker according to https://docs.docker.com/install/linux/docker-ce/debian/
RUN apt-get update && \
    apt-get install -y \
      unzip \
      rsync \
      apt-transport-https \
      ca-certificates \
      curl \
      gnupg2 \
      software-properties-common && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian  $(lsb_release -cs) stable" && \
    apt-get update && \
    apt-cache policy docker-ce && \
    apt-get install -y docker-ce docker-ce-cli containerd.io

# copy config files
COPY --from=opuscapita/minsk-core-ci:1 /root/.grails/settings.groovy /root/.grails/
COPY --from=opuscapita/minsk-core-ci:1 /root/.npmrc /root/

# get install script and pass it to execute: 
RUN curl -sL https://deb.nodesource.com/setup_10.x | bash
# and install node 
RUN apt-get install -y nodejs

# install vault
RUN curl -fsSLk -o /tmp/vault.zip https://releases.hashicorp.com/vault/1.2.2/vault_1.2.2_linux_amd64.zip \
  && unzip /tmp/vault.zip -d /usr/local/bin/ \
  && rm -f /tmp/vault.zip

# install az

# get rid of warnings about missing terminal etc.
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get install ca-certificates curl apt-transport-https lsb-release gnupg \
  && curl -sL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor | tee /etc/apt/trusted.gpg.d/microsoft.asc.gpg > /dev/null \
  && AZ_REPO=$(lsb_release -cs) && echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" | \
    sudo tee /etc/apt/sources.list.d/azure-cli.list \
  && apt-get update && apt-get install azure-cli

# install Kubectl
ARG KUBECTL_VERSION=1.15.3
RUN curl -fLk -o /usr/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl \
  && chmod +x /usr/bin/kubectl

# Install Helm
ARG HELM_VERSION=2.16.1
RUN curl -fLk -o /usr/bin/helm.tar.gz https://storage.googleapis.com/kubernetes-helm/helm-v${HELM_VERSION}-linux-amd64.tar.gz \
  && cd /usr/bin && tar -xzf ./helm.tar.gz --strip-components=1 -C . linux-amd64/helm \
  && chmod +x /usr/bin/helm \
  && rm -rf /usr/bin/helm.tar.gz

# Add 'properties' (https://github.com/OpusCapita/properties) binary file into '/usr/local/bin/'
RUN curl -sL https://git.io/oc-properties | PROPERTIES_TARGET_DIR=/usr/local/bin bash

# we need to replace springlodaded library with the newer one to be able to start 'grails -reloading run-app'
COPY ./springloaded-1.2.8.RELEASE.jar /usr/local/sdkman/candidates/grails/2.4.4/lib/org.springframework/springloaded/jars/
RUN sed -i 's|springloaded-1.2.1.RELEASE.jar|springloaded-1.2.8.RELEASE.jar|g' /usr/local/sdkman/candidates/grails/2.4.4/bin/startGrails
