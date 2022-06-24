FROM tomcat:9-jdk11-openjdk-slim-buster

LABEL maintainer="Jahia Product team <product@jahia.com>"

ARG DEBUG_TOOLS="false"
ARG FFMPEG="false"
ARG LIBREOFFICE="false"
ARG MAVEN_VER="3.8.1"
ARG MAVEN_BASE_URL="https://archive.apache.org/dist/maven/maven-3"
ARG MODULES_BASE_URL="https://store.jahia.com/cms/mavenproxy/private-app-store/org/jahia/modules"
ARG IMAGEMAGICK_BINARIES_DOWNLOAD_URL="https://imagemagick.org/archive/binaries/magick"
ARG LOG_MAX_DAYS="5"
ARG JAHIA_PLUGIN="jahia"
ARG DATA_FOLDER="/var/jahia"
ARG LOGS_FOLDER="/var/log/jahia"

# Container user
ARG C_USER="tomcat"
ARG C_GROUP="tomcat"

ARG INSTALL_GRAALVM=""
ARG GRAALVM_URL="https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java11-linux-amd64-21.3.0.tar.gz"

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
ENV SSL_ENABLED="false" SSL_CERTIFICATE_PASSWD="changeit"
ENV YOURKIT_ACTIVATED="false"

WORKDIR /tmp

RUN apt-get update \
    && magick_packages="libfontconfig1 libx11-6 libharfbuzz0b libfribidi0" \
    && packages="ncat wget unzip logrotate ${magick_packages}" \
    && if ${DEBUG_TOOLS}; then \
        packages="${packages} vim binutils less procps iputils-ping htop"; \
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

RUN if ${INSTALL_GRAALVM:-false}; then \
        echo "Get Graalvm..." \
        && cd /usr/local \
        && wget -nv ${GRAALVM_URL} \
        && tar -xzf graalvm-ce-java*.tar.gz \
        && rm graalvm-ce-java*.tar.gz \
        && mv graalvm* graalvm; \
    fi

ENV GRAALVM_PATH=${INSTALL_GRAALVM:+/usr/local/graalvm}
ENV PATH=/usr/local/graalvm/bin:${PATH}
ENV JAVA_HOME=${GRAALVM_PATH:-$JAVA_HOME}

# Retrieve latest ImageMagick binaries
RUN echo "Retrieve latest ImageMagick binaries..." \
    && wget -nv -O magick ${IMAGEMAGICK_BINARIES_DOWNLOAD_URL} \
    && chmod +x magick \
    && ./magick --appimage-extract \
    && mkdir /opt/magick \
    && mv squashfs-root/usr/* /opt/magick \
    && rm -rf /opt/magick/share/ squashfs-root/ ./magick

RUN echo "Retrieve latest Yourkit agent binaries..." \
    && wget https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2021.11-docker.zip -P /tmp/ \
    && unzip /tmp/YourKit-JavaProfiler-2021.11-docker.zip -d /usr/local \
    && rm /tmp/YourKit-JavaProfiler-2021.11-docker.zip

# Add container user and grant permissions
RUN groupadd -g 999 ${C_GROUP}
RUN useradd -r -u 999 -g ${C_GROUP} ${C_USER} -d /home/${C_USER} -m

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

# Download jahia plugin
USER ${C_USER}
RUN mvn -q -Pconfiguration ${JAHIA_PLUGIN}:help
USER root

# Add scripts
COPY bin/* /usr/local/bin/
COPY setenv.sh /usr/local/tomcat/bin
RUN chmod +x /usr/local/bin/* /usr/local/tomcat/bin/setenv.sh

COPY conf/* /usr/local/tomcat/conf/

COPY target/dependency/shared-libraries /usr/local/tomcat/lib
COPY target/dependency/jdbc-drivers /usr/local/tomcat/lib
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war-data-package /usr/local/tomcat/digital-factory-data
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war /usr/local/tomcat/webapps/ROOT

RUN mkdir -p /etc/jahia  \
    && mkdir -p /etc/jahia/jahia \
    && chown -R ${C_USER}:${C_GROUP} /usr/local/tomcat/conf /etc/jahia

STOPSIGNAL SIGINT

## fix hadolint DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

USER ${C_USER}

EXPOSE 8080
EXPOSE 8101
EXPOSE 8443
EXPOSE 10001

CMD ["/usr/local/bin/entrypoint.sh"]
