# ==============================================================================
# STAGE 1: Base image with GraalVM
# ==============================================================================
FROM tomcat:9-jdk21-temurin as base

# Build feature flags
ARG DEBUG_TOOLS="false"
ARG FFMPEG="false"
ARG LIBREOFFICE="false"
ARG IMAGEMAGICK="false"

# GraalVM configuration
ARG GRAALVM_VERSION
ARG GRAALVM_ARCH
ARG GRAALVM_URL

# Install packages and GraalVM in a single layer to reduce image size
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
    && apt-get install -y --no-install-recommends ${packages} \
    && set -ux \
    && if [ -z "${GRAALVM_ARCH+x}" ]; then \
        GRAALVM_DL_ARCH="$(uname -m)" && \
        if [ "$GRAALVM_DL_ARCH" = "x86_64" ]; then \
            GRAALVM_DL_ARCH="x64" ;\
        else  \
            GRAALVM_DL_ARCH="aarch64" ; \
        fi ; \
    else  \
        GRAALVM_DL_ARCH=$GRAALVM_ARCH ; \
    fi \
    && if [ -z "${GRAALVM_URL+x}" ]; then \
        # GRAALVM_DL_URL=https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAALVM_VERSION}/graalvm-community-jdk-${GRAALVM_VERSION}_linux-${GRAALVM_DL_ARCH}_bin.tar.gz; \
        GRAALVM_DL_URL=https://download.oracle.com/graalvm/${GRAALVM_VERSION%.*.*}/archive/graalvm-jdk-${GRAALVM_VERSION}_linux-${GRAALVM_DL_ARCH}_bin.tar.gz; \
    else  \
        GRAALVM_DL_URL=$GRAALVM_URL ; \
    fi \
    && echo "GraalVM download url ${GRAALVM_DL_URL}" \
    && cd /usr/local \
    && GRAALVM_SHA256="$(curl --no-progress-meter -L "$GRAALVM_DL_URL".sha256)" \
    && curl --no-progress-meter -L --output /tmp/graalvm.tgz "$GRAALVM_DL_URL" \
    && echo "$GRAALVM_SHA256" /tmp/graalvm.tgz | sha256sum -c \
    && mkdir -p "/usr/local/graalvm" \
    && tar --extract --file /tmp/graalvm.tgz --directory "/usr/local/graalvm" --strip-components 1 \
    && /usr/local/graalvm/bin/gu install js \
    # Clean up GraalVM installation and remove openjdk
    && rm -f /tmp/graalvm.tgz \
    && rm -rf /usr/local/openjdk-21/ \
    && rm -rf /opt/java/openjdk \
    # Clean up apt cache and temporary files
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    # Clean up Tomcat webapps examples
    && rm -rf /usr/local/tomcat/webapps.dist \
    # Cleanup JDK /jmods and /lib/src.zip not needed for runtime
    && rm -rf /usr/local/graalvm/jmods \
    && rm -rf /usr/local/graalvm/lib/src.zip

# Set up environment variables for GraalVM
ENV PATH="/usr/local/graalvm/bin:${PATH}" \
    JAVA_HOME="/usr/local/graalvm"

# ==============================================================================
# STAGE 2: Final application image
# ==============================================================================
FROM scratch
COPY --from=base / /

ARG MAVEN_VER="3.9.9"
ARG MAVEN_BASE_URL="https://archive.apache.org/dist/maven/maven-3"
ARG MODULES_BASE_URL="https://store.jahia.com/cms/mavenproxy/private-app-store/org/jahia/modules"
ARG IMAGEMAGICK_BINARIES_DOWNLOAD_URL="https://imagemagick.org/archive/binaries/magick"
ARG DATA_FOLDER="/var/jahia"
ARG ETC_FOLDER="/etc/jahia"
ARG LOGS_FOLDER="/var/log/jahia"
ARG CATALINA_HOME="/usr/local/tomcat"
ARG LOG_MAX_DAYS="5"
ARG TARGETOS
ARG TARGETPLATFORM
ARG TARGETARCH
ARG TARGETVARIANT
ARG C_USER="tomcat"
ARG C_GROUP="tomcat"

ENV RESTORE_MODULE_STATES="false" \
    RESTORE_PERSISTED_CONFIGURATION="false" \
    PROCESSING_SERVER="true" \
    OPERATING_MODE="development" \
    MAX_UPLOAD="268435456" \
    MAX_RAM_PERCENTAGE="80" \
    JAHIA_LICENSE="" \
    JAHIA_PROPERTIES="" \
    JAHIA_DISABLE_SNAPSHOTS_PULL="false" \
    EXECUTE_PROVISIONING_SCRIPT="" \
    DATA_FOLDER="${DATA_FOLDER}" \
    ETC_FOLDER="${ETC_FOLDER}" \
    LOGS_FOLDER="${LOGS_FOLDER}" \
    CATALINA_BASE="${CATALINA_HOME}" \
    CATALINA_HOME="${CATALINA_HOME}" \
    CATALINA_TMPDIR="${CATALINA_HOME}/temp" \
    CATALINA_CONTEXT="ROOT" \
    MAVEN_OPTS="-Xmx256m" \
    MAVEN_VER="${MAVEN_VER}" \
    PATH="/usr/local/graalvm/bin:${PATH}" \
    JAVA_HOME="/usr/local/graalvm" \
    LOG_MAX_DAYS="${LOG_MAX_DAYS}" \
    LOG_MAX_SIZE="500M" \
    CATALINA_OPTS="" \
    JPDA="false" \
    JPDA_ADDRESS="*:8000" \
    DB_VENDOR="derby_embedded" \
    DB_HOST="mariadb" \
    DB_NAME="jahia" \
    DB_USER="jahia" \
    DB_PASS="fakepassword" \
    DB_PORT="" \
    DB_URL="" \
    OVERWRITEDB="if-necessary" \
    SUPER_USER_PASSWORD="root1234" \
    DS_IN_AWS="false" \
    DS_IN_DB="true" \
    DS_PATH="" \
    SSL_ENABLED="false" \
    SSL_CERTIFICATE_PASSWD="changeit" \
    YOURKIT_ACTIVATED="false"

# System setup and application preparation
RUN printf "I'm building for TARGETPLATFORM=${TARGETPLATFORM}" \
    && printf ", TARGETOS=${TARGETOS}" \
    && printf ", TARGETARCH=${TARGETARCH}" \
    && printf ", TARGETVARIANT=${TARGETVARIANT} \n" \
    && printf "With uname -s : " && uname -s \
    && printf "and  uname -m : " && uname -m \
    # User and group setup
    && groupadd -g 9999 ${C_GROUP} \
    && useradd -r -u 9999 -g ${C_GROUP} ${C_USER} -d /home/${C_USER} -m \
    # Application directories setup
    && mkdir -p ${DATA_FOLDER}/info ${DATA_FOLDER}/repository ${DATA_FOLDER}/modules ${DATA_FOLDER}/patches/provisioning ${LOGS_FOLDER} \
    && chown -R ${C_USER}:${C_GROUP} ${DATA_FOLDER} ${LOGS_FOLDER} \
    && rm -r ${CATALINA_HOME}/logs \
    && ln -s ${LOGS_FOLDER} ${CATALINA_HOME}/logs \
    # Maven installation
    && wget -nv -O maven.zip ${MAVEN_BASE_URL}/${MAVEN_VER}/binaries/apache-maven-${MAVEN_VER}-bin.zip \
    && unzip maven.zip -d /opt \
    && ln -s /opt/apache-maven-${MAVEN_VER}/bin/mvn /usr/local/bin/mvn \
    && rm maven.zip \
    # Additional directories
    && mkdir -p /opt/jahia \
    && mkdir -p ${ETC_FOLDER} \
    && mkdir -p ${ETC_FOLDER}/jahia \
    && chown -R ${C_USER}:${C_GROUP} ${CATALINA_HOME}/conf ${ETC_FOLDER}

# Application finalization
COPY --chown=${C_USER}:${C_GROUP} settings.xml /home/${C_USER}/.m2/settings.xml
COPY conf/* ${CATALINA_HOME}/conf/
COPY bin/* /usr/local/bin/
COPY setenv.sh ${CATALINA_HOME}/bin
RUN chmod +x /usr/local/bin/* ${CATALINA_HOME}/bin/setenv.sh
COPY target/dependency/shared-libraries ${CATALINA_HOME}/lib
COPY target/dependency/jdbc-drivers ${CATALINA_HOME}/lib
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war-data-package ${CATALINA_HOME}/digital-factory-data
# During the build, simply copy the Jahia WAR folder to a temp folder (jahia-war/). At runtime, this folder will be copied to the deployment folder (under webapps/) specified by the CATALINA_CONTEXT env variable
COPY --chown=${C_USER}:${C_GROUP} target/dependency/jahia-war ${CATALINA_HOME}/jahia-war
# Grant permissions to the webapps/ folder so that the tomcat user can copy the Jahia WAR folder in it at runtime
RUN chown -R ${C_USER}:${C_GROUP} ${CATALINA_HOME}/webapps
COPY --chown=${C_USER}:${C_GROUP} target/configurator/configurator.jar /opt/jahia

# Shell (fix hadolint DL4006) and runtime configuration
STOPSIGNAL SIGINT
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
WORKDIR /tmp
USER ${C_USER}

# Port definitions
# 9229: GraalVM Chrome debugger
# 10001: YourKit profiling
EXPOSE 8000 8080 8101 8443 9229 10001

# Application entry point
CMD ["/usr/local/bin/entrypoint.sh"]
