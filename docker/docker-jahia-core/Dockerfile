FROM tomcat:9-jdk21-temurin as base

ARG DEBUG_TOOLS="false"
ARG FFMPEG="false"
ARG LIBREOFFICE="false"
ARG IMAGEMAGICK="false"

ARG GRAALVM_VERSION
# We provide these arguments to be able to override the "guessing" that is done in the script below in case of problems
ARG GRAALVM_ARCH
ARG GRAALVM_URL

RUN rm -Rf /usr/local/openjdk-21/

RUN apt-get update \
    && apt-get upgrade -y \
    && packages="apt-utils curl ncat wget unzip logrotate" \
    && if ${DEBUG_TOOLS}; then \
        packages="${packages} vim binutils less procps iputils-ping htop"; \
       fi \
    && if ${IMAGEMAGICK}; then \
        packages="${packages} libfontconfig1 libx11-6 libharfbuzz0b libfribidi0 imagemagick"; \
       fi \
    && if ${LIBREOFFICE}; then \
        packages="${packages} libreoffice"; \
       fi \
    && if ${FFMPEG}; then \
        packages="${packages} ffmpeg"; \
       fi \
    && apt-get install -y --no-install-recommends \
        ${packages} \
    && rm -rf /var/lib/apt/lists/*

ENV PATH="/usr/local/graalvm/bin:${PATH}"
ENV JAVA_HOME=/usr/local/graalvm

RUN set -ux; \
    if [ -z ${GRAALVM_ARCH+x} ]; then \
        GRAALVM_DL_ARCH="$(uname -m)" && \
        if [ "$GRAALVM_DL_ARCH" = "x86_64" ]; then \
            GRAALVM_DL_ARCH="x64" ;\
        else  \
            GRAALVM_DL_ARCH="aarch64" ; \
        fi ; \
    else  \
        GRAALVM_DL_ARCH=$GRAALVM_ARCH ; \
    fi; \
    if [ -z ${GRAALVM_URL+x} ]; then \
         # GRAALVM_DL_URL=https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAALVM_VERSION}/graalvm-community-jdk-${GRAALVM_VERSION}_linux-${GRAALVM_DL_ARCH}_bin.tar.gz; \
            GRAALVM_DL_URL=https://download.oracle.com/graalvm/${GRAALVM_VERSION%.*.*}/archive/graalvm-jdk-${GRAALVM_VERSION}_linux-${GRAALVM_DL_ARCH}_bin.tar.gz; \
    else  \
        GRAALVM_DL_URL = $GRAALVM_URL ; \
    fi ; \
    echo "GraalVM download url ${GRAALVM_DL_URL}" ; \
    cd /usr/local && \
    GRAALVM_SHA256="$(curl --no-progress-meter -L "$GRAALVM_DL_URL".sha256)" && \
    curl --no-progress-meter -L --output /tmp/graalvm.tgz "$GRAALVM_DL_URL" && \
    echo "$GRAALVM_SHA256" */tmp/graalvm.tgz | sha256sum -c && \
    mkdir -p "/usr/local/graalvm" && \
    tar --extract --file /tmp/graalvm.tgz --directory "/usr/local/graalvm" --strip-components 1 && \
    rm /tmp/graalvm.tgz && \
    /usr/local/graalvm/bin/gu install js ;

# Wrap everything to reduce the image size from the removed elements
FROM scratch
COPY --from=base / /

ARG MAVEN_VER="3.9.9"
ARG MAVEN_BASE_URL="https://archive.apache.org/dist/maven/maven-3"
ARG MODULES_BASE_URL="https://store.jahia.com/cms/mavenproxy/private-app-store/org/jahia/modules"
ARG IMAGEMAGICK_BINARIES_DOWNLOAD_URL="https://imagemagick.org/archive/binaries/magick"
ARG LOG_MAX_DAYS="5"
ARG JAHIA_PLUGIN="jahia"
ARG DATA_FOLDER="/var/jahia"
ARG LOGS_FOLDER="/var/log/jahia"
ARG TARGETOS
ARG TARGETPLATFORM
ARG TARGETARCH
ARG TARGETVARIANT


# Container user
ARG C_USER="tomcat"
ARG C_GROUP="tomcat"

ENV RESTORE_MODULE_STATES="false"
ENV RESTORE_PERSISTED_CONFIGURATION="false"
ENV DATA_FOLDER=${DATA_FOLDER} LOGS_FOLDER=${LOGS_FOLDER}
ENV PROCESSING_SERVER="true"
ENV OPERATING_MODE="development"
ENV MAX_UPLOAD="268435456"
ENV MAX_RAM_PERCENTAGE="80"
ENV MAVEN_OPTS="-Xmx256m"
ENV MAVEN_VER="${MAVEN_VER}"
ENV JAHIA_PLUGIN="${JAHIA_PLUGIN}"
ENV JAHIA_LICENSE=""

ENV CATALINA_BASE="/usr/local/tomcat" CATALINA_HOME="/usr/local/tomcat" CATALINA_TMPDIR="/usr/local/tomcat/temp" LOG_MAX_DAYS="5" LOG_MAX_SIZE="500M"
ENV CATALINA_OPTS="" JPDA="false" JPDA_ADDRESS="*:8000"

ENV DB_VENDOR="derby_embedded" DB_HOST="mariadb" DB_NAME="jahia" DB_USER="jahia" DB_PASS="fakepassword" DB_PORT="" DB_URL=""
ENV OVERWRITEDB="if-necessary"
ENV SUPER_USER_PASSWORD="root1234"
ENV DS_IN_AWS="false"
ENV DS_IN_DB="true" DS_PATH=""
ENV EXECUTE_PROVISIONING_SCRIPT=""
ENV JAHIA_PROPERTIES=""
ENV JAHIA_DISABLE_SNAPSHOTS_PULL="false"
ENV SSL_ENABLED="false" SSL_CERTIFICATE_PASSWD="changeit"
ENV YOURKIT_ACTIVATED="false"
ENV PATH="/usr/local/graalvm/bin:${PATH}"
ENV JAVA_HOME=/usr/local/graalvm

WORKDIR /tmp

RUN printf "I'm building for TARGETPLATFORM=${TARGETPLATFORM}" \
    && printf ", TARGETOS=${TARGETOS}" \
    && printf ", TARGETARCH=${TARGETARCH}" \
    && printf ", TARGETVARIANT=${TARGETVARIANT} \n" \
    && printf "With uname -s : " && uname -s \
    && printf "and  uname -m : " && uname -m

# Add container user and grant permissions
RUN groupadd -g 9999 ${C_GROUP}
RUN useradd -r -u 9999 -g ${C_GROUP} ${C_USER} -d /home/${C_USER} -m

# Prepare data folders
RUN mkdir -p ${DATA_FOLDER}/info ${DATA_FOLDER}/repository ${DATA_FOLDER}/modules ${DATA_FOLDER}/patches/provisioning ${LOGS_FOLDER} \
    && chown -R ${C_USER}:${C_GROUP} ${DATA_FOLDER} ${LOGS_FOLDER} \
    && rm -r /usr/local/tomcat/logs \
    && ln -s ${LOGS_FOLDER} /usr/local/tomcat/logs

# Prepare maven
RUN wget -nv -O maven.zip ${MAVEN_BASE_URL}/${MAVEN_VER}/binaries/apache-maven-${MAVEN_VER}-bin.zip \
    && unzip maven.zip -d /opt \
    && ln -s /opt/apache-maven-${MAVEN_VER}/bin/mvn /usr/local/bin/mvn \
    && rm maven.zip

COPY --chown=${C_USER}:${C_GROUP} settings.xml /home/${C_USER}/.m2/settings.xml

# Add scripts
COPY bin/* /usr/local/bin/
COPY setenv.sh /usr/local/tomcat/bin
RUN chmod +x /usr/local/bin/* /usr/local/tomcat/bin/setenv.sh

COPY conf/* /usr/local/tomcat/conf/

COPY target/dependency/shared-libraries /usr/local/tomcat/lib
COPY target/dependency/jdbc-drivers /usr/local/tomcat/lib
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war-data-package /usr/local/tomcat/digital-factory-data
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war /usr/local/tomcat/webapps/ROOT

# Copy configurator
RUN mkdir -p /opt/jahia
COPY --chown=${C_USER}:${C_GROUP} target/configurator/configurator.jar /opt/jahia

RUN mkdir -p /etc/jahia  \
    && mkdir -p /etc/jahia/jahia \
    && chown -R ${C_USER}:${C_GROUP} /usr/local/tomcat/conf /etc/jahia

STOPSIGNAL SIGINT

## fix hadolint DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

USER ${C_USER}

EXPOSE 8000
EXPOSE 8080
EXPOSE 8101
EXPOSE 8443

# This port is used to be able to access the GraalVM Chrome debugger tool : https://www.graalvm.org/latest/tools/chrome-debugger/
EXPOSE 9229

# This port is used for yourkit profiling
EXPOSE 10001

CMD ["/usr/local/bin/entrypoint.sh"]
