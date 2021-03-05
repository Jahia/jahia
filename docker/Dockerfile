FROM tomcat:9-jdk11-openjdk-slim-buster

LABEL maintainer="Jahia Product team <product@jahia.com>"

ARG DEBUG_TOOLS="false"
ARG FFMPEG="false"
ARG LIBREOFFICE="false"
ARG MAVEN_VER="3.6.3"
ARG MAVEN_BASE_URL="https://mirrors.ircam.fr/pub/apache/maven/maven-3"
ARG MODULES_BASE_URL="https://store.jahia.com/cms/mavenproxy/private-app-store/org/jahia/modules"
ARG IMAGEMAGICK_BINARIES_DOWNLOAD_URL="https://imagemagick.org/download/binaries/magick"
ARG LOG_MAX_DAYS="5"
ARG JAHIA_PLUGIN="org.jahia.server:jahia-maven-plugin:5.12-SNAPSHOT"

ENV RESTORE_MODULE_STATES="false"
ENV RESTORE_PERSISTED_CONFIGURATION="false"
ENV FACTORY_DATA="/data/digital-factory-data"
ENV FACTORY_CONFIG="/usr/local/tomcat/conf/digital-factory-config"
ENV PROCESSING_SERVER="true"
ENV OPERATING_MODE="development"
ENV MAX_UPLOAD="268435456"
ENV MAX_RAM_PERCENTAGE="95"
ENV MAVEN_OPTS="-Xmx256m"
ENV MAVEN_VER="$MAVEN_VER"
ENV JAHIA_PLUGIN="$JAHIA_PLUGIN"

ENV CATALINA_BASE="/usr/local/tomcat" CATALINA_HOME="/usr/local/tomcat" CATALINA_TMPDIR="/usr/local/tomcat/temp" LOG_MAX_DAYS="5"
ENV CATALINA_OPTS="" JPDA="false" JPDA_ADDRESS="*:8000"

ENV DB_VENDOR="derby_embedded" DB_HOST="mariadb" DB_NAME="jahia" DB_USER="jahia" DB_PASS="fakepassword"
ENV SUPER_USER_PASSWORD="root1234"
ENV DS_IN_DB="true" DS_PATH="/datastore/jahia"

WORKDIR /tmp

# I removed : "python3 jq libx11-6 libharfbuzz0b libfribidi0"
# jq needed for healtcheck, python3 for old password script (removed) - others to be checked ?

RUN apt-get update \
    && packages="ncat wget unzip" \
    && if $DEBUG_TOOLS; then \
        packages="$packages vim binutils"; \
       fi \
    && if $LIBREOFFICE; then \
        packages="$packages libreoffice"; \
       fi \
    && if $FFMPEG; then \
        packages="$packages ffmpeg"; \
       fi \
    && apt-get install -y --no-install-recommends \
        $packages \
    && rm -rf /var/lib/apt/lists/*

# Retrieve latest ImageMagick binaries
RUN echo "Retrieve latest ImageMagick binaries..." \
    && wget -nv -O magick $IMAGEMAGICK_BINARIES_DOWNLOAD_URL \
    && chmod +x magick \
    && ./magick --appimage-extract \
    && mkdir /opt/magick \
    && mv squashfs-root/usr/* /opt/magick \
    && rm -rf /opt/magick/share/ squashfs-root/ ./magick

COPY settings.xml /root/.m2/settings.xml

RUN wget -nv -O maven.zip $MAVEN_BASE_URL/$MAVEN_VER/binaries/apache-maven-$MAVEN_VER-bin.zip \
    && unzip maven.zip -d /opt \
    && ln -s /opt/apache-maven-${MAVEN_VER}/bin/mvn /usr/local/bin/mvn \
    && rm maven.zip

# Download jahia plugin
RUN mvn -q -Pconfiguration ${JAHIA_PLUGIN}:help

COPY entrypoint.sh /
COPY downloadProvisioning.sh /usr/local/bin

COPY target/dependency/shared-libraries /usr/local/tomcat/lib
COPY target/dependency/jdbc-drivers /usr/local/tomcat/lib
COPY target/dependency/jahia-war-data-package /data/digital-factory-data
COPY target/dependency/jahia-war /usr/local/tomcat/webapps/ROOT

RUN printf "Start Jahia's installation...\n" \
RUN chmod +x /entrypoint.sh \
RUN mkdir -p /data/digital-factory-data/modules /data/digital-factory-data/patches/provisioning
RUN sed -e "s/\"org.apache.catalina.startup.VersionLoggerListener\"/\"org.apache.catalina.startup.VersionLoggerListener\" logArgs=\"false\"/g" -i /usr/local/tomcat/conf/server.xml
RUN sed -e "s#common.loader=\"\\\$#common.loader=\"/usr/local/tomcat/conf/digital-factory-config\",\"\$#g" -i /usr/local/tomcat/conf/catalina.properties \
    && sed -e 's/tomcat.util.scan.StandardJarScanFilter.jarsToSkip=/tomcat.util.scan.StandardJarScanFilter.jarsToSkip=derby*.jar,mysql-connector-java-*.jar,ojdbc*.jar,orai18n-*.jar,postgresql-*.jar,sqljdbc*.jar,mssql-jdbc-*.jar,mariadb-java-client-*.jar/' -i /usr/local/tomcat/conf/catalina.properties \
    && echo

## fix hadolint DL4006
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

EXPOSE 8080
EXPOSE 7860
EXPOSE 8101

CMD ["/entrypoint.sh"]
