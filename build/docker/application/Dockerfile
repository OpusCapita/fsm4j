FROM alpine:3.7 as artifacts

RUN apk update && apk add unzip bash curl ca-certificates tar

# Unzip application

ARG WAR_PATH
COPY $WAR_PATH /tmp/app.zip
RUN mkdir /var/app && unzip -q -x /tmp/app.zip -d /var/app

# Download Tomcat from Apache website

ARG TOMCAT_VERSION=7.0.92
RUN curl -fLk -o /tmp/tomcat.tar.gz https://archive.apache.org/dist/tomcat/tomcat-7/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz
RUN mkdir /usr/share/tomcat \
    && tar -xzf /tmp/tomcat.tar.gz --strip-components=1 -C /usr/share/tomcat \
    && rm -rf /usr/share/tomcat/webapps/*

# Build application image

FROM opuscapita/minsk-core-oracle-jre:1.8.192

ARG NAME
ARG CREATED
ARG SOURCE
ARG REVISION
ARG VERSION

LABEL maintainer="kirill.volkovich@opuscapita.com" \
      org.opencontainers.image.created="$CREATED" \
      org.opencontainers.image.source="$SOURCE" \
      org.opencontainers.image.revision="$REVISION" \
      org.opencontainers.image.vendor="OpusCapita" \
      org.opencontainers.image.version="$VERSION"

ENV JAVA_OPTS " -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Xms2g -Xmx2g"
ENV APP_NAME=$NAME
ENV APP_VERSION=$VERSION

# Copy application

ENV APP_ROOT="/var/app"
COPY --from=artifacts /var/app/ ${APP_ROOT}

# Setup tomcat

ENV TZ=Europe/Minsk
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV LANG="C.UTF-8"
ENV CATALINA_HOME="/usr/lib/tomcat"

WORKDIR $CATALINA_HOME
COPY --from=artifacts /usr/share/tomcat ./
RUN ln -s ${CATALINA_HOME}/bin/catalina.sh /usr/bin/catalina

# Install XmlStarlet required for editing Tomcat configuration,
# and configure server.xml initially
RUN apt-get update && apt-get install xmlstarlet -y

# updating server.xml
RUN tmp=$(mktemp) \
    && cat ${CATALINA_HOME}/conf/server.xml \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "enableLookups" -v "true" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "URIEncoding" -v "UTF-8" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "useBodyEncodingForURI" -v "true" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "maxHttpHeaderSize" -v "32768" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "compression" -v "on" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "compressableMimeType" -v "text/html,application/xhtml+xml,application/json,text/json" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Connector[@port='8080']" -t attr -n "relaxedQueryChars" -v "[,],{,},|" \
    > $tmp \
    && cat $tmp > ${CATALINA_HOME}/conf/server.xml

# adding RemoteIpValve, so Tomcat gets correct client IP (not the proxy one)
RUN tmp=$(mktemp) \
    && cat $CATALINA_HOME/conf/server.xml \
    | xmlstarlet ed -s "Server/Service[@name='Catalina']/Engine[@name='Catalina']" -t elem -n "Valve" \
    | xmlstarlet ed -i "Server/Service[@name='Catalina']/Engine[@name='Catalina']/Valve" -t attr -n "className" -v "org.apache.catalina.valves.RemoteIpValve" \
    > $tmp \
    && cat $tmp > $CATALINA_HOME/conf/server.xml

COPY /build/docker/application/bin/ /usr/local/bin/

# Add 'properties' (https://github.com/OpusCapita/properties) binary file into '/usr/local/bin/'
RUN curl -sL https://git.io/oc-properties | PROPERTIES_TARGET_DIR=/usr/local/bin bash

VOLUME /etc/conf.d

RUN ln -s ${APP_ROOT}/WEB-INF/conf /etc/conf.d

EXPOSE 8080

ENTRYPOINT ["entrypoint.sh"]
