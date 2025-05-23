#!/bin/bash

if [ ! -f "/usr/local/tomcat/conf/configured" ]; then
    echo "Initial container startup, configuring Jahia..."

    if [ -f "${DATA_FOLDER}/info/version.properties" ] || [ -d "${DATA_FOLDER}/bundles-deployed" ]; then
        echo "Previous installation detected. Do not override db and existing data. Checking write permissions..."
        PREVIOUS_INSTALL="true"
        OVERWRITEDB="false"

        if ! find "$DATA_FOLDER" "$LOGS_FOLDER" -type f -not -writable | grep -q .; then
            echo "All files are writable."
        else
            echo "Error: Some files are not writable." >&2
            find "$DATA_FOLDER" "$LOGS_FOLDER" -type f -not -writable -print
            echo "This could be caused by a docker image upgrade. Please follow the migration steps at the academy: https://academy.jahia.com/documentation/jahia/jahia-8/dev-ops/docker/migrations-with-docker-images"
            exit 1
        fi
    else
        PREVIOUS_INSTALL="false"
    fi

    if [ "$PROCESSING_SERVER" == "false" ]; then
        OVERWRITEDB="false"
    fi

    if [ -f "${DATA_FOLDER}/env" ]; then
        source ${DATA_FOLDER}/env
    else
        echo "Storing environment to prevent changes"
        echo "DB_VENDOR=${DB_VENDOR}" >> ${DATA_FOLDER}/env
        echo "DB_HOST=${DB_HOST}" >> ${DATA_FOLDER}/env
        echo "DB_NAME=${DB_NAME}" >> ${DATA_FOLDER}/env
        echo "DB_USER=${DB_USER}" >> ${DATA_FOLDER}/env
        echo "DB_PASS=${DB_PASS}" >> ${DATA_FOLDER}/env
        echo "DS_IN_AWS=${DS_IN_AWS}" >> ${DATA_FOLDER}/env
        echo "DS_IN_DB=${DS_IN_DB}" >> ${DATA_FOLDER}/env
        echo "DS_PATH=${DS_PATH}" >> ${DATA_FOLDER}/env
    fi

    echo "Updating digital-factory-data..."
    cp -a /usr/local/tomcat/digital-factory-data/* ${DATA_FOLDER}/

    echo "Updating /usr/local/tomcat/conf/server.xml and logging.properties..."
    sed -i "s|#LOGS_FOLDER#|$LOGS_FOLDER|g" /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/logging.properties

    if [ "$SSL_ENABLED" == "true" ]; then
      if [ ! -d /usr/local/tomcat/conf/ssl ]; then
        mkdir -p /usr/local/tomcat/conf/ssl
        openssl req -x509 -newkey rsa:4096 -keyout /usr/local/tomcat/conf/ssl/localhost-rsa-key.pem -out /usr/local/tomcat/conf/ssl/localhost-rsa-cert.pem -days 36500 -subj "/CN=localhost" -passout env:SSL_CERTIFICATE_PASSWD
      fi

      sed -i '/#SSL_DISABLED#/d' /usr/local/tomcat/conf/server.xml
      sed -i "s/#SSL_CERTIFICATE_PASSWD#/${SSL_CERTIFICATE_PASSWD}/" /usr/local/tomcat/conf/server.xml
    fi

    echo "Update log4j..."
    sed -i 's/ref="RollingJahia/ref="Jahia/' /usr/local/tomcat/webapps/ROOT/WEB-INF/etc/config/log4j2.xml

    sed -i "s|#LOGS_FOLDER#|$LOGS_FOLDER|g;s|#LOG_MAX_DAYS#|$LOG_MAX_DAYS|g;s|#LOG_MAX_SIZE#|$LOG_MAX_SIZE|g" /usr/local/tomcat/conf/jahia_logrotate

    if [ "$DB_URL" == "" ]; then
      case "$DB_VENDOR" in
          "mysql")
              DB_PORT=${DB_PORT:-3306}
              DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/jahia?characterEncoding=UTF-8&sslMode=DISABLED&allowPublicKeyRetrieval=true"
              ;;
          "mariadb")
              DB_PORT=${DB_PORT:-3306}
              DB_URL="jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&amp;characterEncoding=UTF-8&amp;useServerPrepStmts=false&amp;useSSL=false"
              ;;
          "postgresql")
              DB_PORT=${DB_PORT:-5432}
              DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
              ;;
          "mssql")
              DB_PORT=${DB_PORT:-1433}
              DB_URL="jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME}"
              ;;
          "oracle")
              DB_PORT=${DB_PORT:-1521}
              DB_URL="jdbc:oracle:thin:@${DB_HOST}:${DB_PORT}:${DB_NAME}"
              ;;
          "derby")
              DB_PORT=${DB_PORT:-1527}
              DB_URL="jdbc:derby://${DB_HOST}:${DB_PORT}/${DB_NAME}"
              DS_IN_DB=false
              ;;
          "derby_embedded")
              DB_URL="jdbc:derby:directory:${DATA_FOLDER}/jahiadb;create=true"
              DS_IN_DB=false
              ;;
      esac
    fi
    echo "Using database URL: ${DB_URL}"

    if [ "${DB_HOST}" != "" ] && [ "${DB_PORT}" != "" ]; then
      alive.sh ${DB_HOST} ${DB_PORT}
    fi

    if [ "${JAHIA_LICENSE}" != "" ]; then
      echo "decoding license"
      echo "${JAHIA_LICENSE}" | base64 --decode > ${DATA_FOLDER}/license.xml
      JAHIA_LICENSE_OPTS="-Djahia.configure.licenseFile=${DATA_FOLDER}/license.xml"
    else
      echo "No license provided via environment variable"
    fi

    if [ "${JAHIA_PROPERTIES}" != "" ]; then
      JAHIA_PROPERTIES="${JAHIA_PROPERTIES},\"mvnPath\":\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",\"svnPath\":\"/usr/bin/svn\",\"gitPath\":\"/usr/bin/git\",\"karaf.remoteShell.host\":\"0.0.0.0\""
    else
      JAHIA_PROPERTIES="\"mvnPath\":\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",\"svnPath\":\"/usr/bin/svn\",\"gitPath\":\"/usr/bin/git\",\"karaf.remoteShell.host\":\"0.0.0.0\""
    fi

    echo "Configure jahia..."
    echo "/opt/apache-maven-${MAVEN_VER}/bin/mvn ${JAHIA_PLUGIN}:configure \
    -Djahia.deploy.targetServerType="tomcat" \
    -Djahia.deploy.targetServerDirectory="/usr/local/tomcat" \
    -Djahia.deploy.dataDir="${DATA_FOLDER}" \
    -Djahia.configure.externalizedTargetPath="/etc/jahia" \
    -Djahia.configure.databaseType="${DB_VENDOR}" \
    -Djahia.configure.databaseUrl="${DB_URL}" \
    -Djahia.configure.databaseUsername="${DB_USER}" \
    -Djahia.configure.databasePassword=xxxxx \
    -Djahia.configure.storeFilesInAWS="${DS_IN_AWS}" \
    -Djahia.configure.storeFilesInDB="${DS_IN_DB}" \
    -Djahia.configure.fileDataStorePath="${DS_PATH}" \
    -Djahia.configure.jahiaRootPassword=xxxxx \
    -Djahia.configure.processingServer="${PROCESSING_SERVER}" \
    -Djahia.configure.operatingMode="${OPERATING_MODE}" \
    -Djahia.configure.deleteFiles="false" \
    -Djahia.configure.overwritedb="${OVERWRITEDB}" \
    -Djahia.configure.jahiaProperties="{${JAHIA_PROPERTIES}}" \
    $JAHIA_CONFIGURE_OPTS $JAHIA_LICENSE_OPTS -Pconfiguration"

    /opt/apache-maven-${MAVEN_VER}/bin/mvn ${JAHIA_PLUGIN}:configure \
    -Djahia.deploy.targetServerType="tomcat" \
    -Djahia.deploy.targetServerDirectory="/usr/local/tomcat" \
    -Djahia.deploy.dataDir="${DATA_FOLDER}" \
    -Djahia.configure.externalizedTargetPath="/etc/jahia" \
    -Djahia.configure.databaseType="${DB_VENDOR}" \
    -Djahia.configure.databaseUrl="${DB_URL}" \
    -Djahia.configure.databaseUsername="${DB_USER}" \
    -Djahia.configure.databasePassword="${DB_PASS}" \
    -Djahia.configure.storeFilesInAWS="${DS_IN_AWS}" \
    -Djahia.configure.storeFilesInDB="${DS_IN_DB}" \
    -Djahia.configure.fileDataStorePath="${DS_PATH}" \
    -Djahia.configure.jahiaRootPassword="${SUPER_USER_PASSWORD}" \
    -Djahia.configure.processingServer="${PROCESSING_SERVER}" \
    -Djahia.configure.operatingMode="${OPERATING_MODE}" \
    -Djahia.configure.deleteFiles="false" \
    -Djahia.configure.overwritedb="${OVERWRITEDB}" \
    -Djahia.configure.jahiaProperties="{${JAHIA_PROPERTIES}}" \
    $JAHIA_CONFIGURE_OPTS $JAHIA_LICENSE_OPTS -Pconfiguration

    mkdir -p ${DATA_FOLDER}/info/initial-config
    cp /etc/jahia/jahia/* ${DATA_FOLDER}/info/initial-config

    if [ -f "${DATA_FOLDER}/info/passwd" ] && [ "`cat "${DATA_FOLDER}/info/passwd"`" != "`echo -n "$SUPER_USER_PASSWORD" | sha256sum`" ]; then
        echo "Update root's password..."
        echo "${SUPER_USER_PASSWORD}" > ${DATA_FOLDER}/root.pwd
    fi

    if [ "${EXECUTE_PROVISIONING_SCRIPT}" != "" ]; then
      echo " - include: ${EXECUTE_PROVISIONING_SCRIPT}" > ${DATA_FOLDER}/patches/provisioning/999-docker-provisioning.yaml
    fi

    echo -n "${SUPER_USER_PASSWORD}" | sha256sum > ${DATA_FOLDER}/info/passwd

    touch "/usr/local/tomcat/conf/configured"
fi

if [[ $CATALINA_OPTS != *"-Djava.security.egd"* ]]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djava.security.egd=file:/dev/urandom"
fi

if [[ $CATALINA_OPTS != *"-Djahia.log.dir"* ]]; then
    export CATALINA_OPTS="${CATALINA_OPTS} -Djahia.log.dir=${LOGS_FOLDER}"
fi

if [ "${RESTORE_MODULE_STATES}" == "true" ]; then
    echo " -- Restore module states have been asked"
    touch "${DATA_FOLDER}/[persisted-bundles].dorestore"
fi

if [ "${RESTORE_PERSISTED_CONFIGURATION}" == "true" ]; then
    echo " -- Restore OSGi configuration have been asked"
    touch "${DATA_FOLDER}/[persisted-configurations].dorestore"
fi

if [ "$YOURKIT_ACTIVATED" == "true" ]; then
    if [ ! -d "/home/tomcat/yourkit" ]; then
        echo "Retrieve Yourkit agent binaries..."
        wget -nv -O yourkit.zip https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2025.3-docker.zip -P /tmp/
        unzip /tmp/yourkit.zip -d /home/tomcat
        mv /home/tomcat/YourKit-JavaProfiler* /home/tomcat/yourkit
        rm /tmp/yourkit.zip
    fi

    case $(uname -m) in

    x86_64)
      export CATALINA_OPTS="${CATALINA_OPTS} -agentpath:/home/tomcat/yourkit/bin/linux-x86-64/libyjpagent.so=port=10001,disableall,listen=all"
      ;;

    aarch64)
      export CATALINA_OPTS="${CATALINA_OPTS} -agentpath:/home/tomcat/yourkit/bin/linux-arm-64/libyjpagent.so=port=10001,disableall,listen=all"
      ;;
    esac
fi

if [ "$JAHIA_DISABLE_SNAPSHOTS_PULL" == "true" ]; then
  echo "Snapshots from Karaf's Jahia remote maven repository are disabled"
  sed -i 's/public@id=jahia-public@snapshots/public@id=jahia-public/g' ${DATA_FOLDER}/karaf/etc/org.ops4j.pax.url.mvn.cfg
else
  echo "Snapshots from Karaf's Jahia remote maven repository are enabled"
  sed -i 's/public@id=jahia-public$/public@id=jahia-public@snapshots/g' ${DATA_FOLDER}/karaf/etc/org.ops4j.pax.url.mvn.cfg
fi

if [ "$JPDA" == "true" ]; then
  OPT="jpda run"
else
  OPT="run"
fi

export CATALINA_OPTS="${CATALINA_OPTS}"
export JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS --add-opens=java.base/java.net=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.instrumentation=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.exception=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.object=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.strings=ALL-UNNAMED --add-exports org.graalvm.truffle/com.oracle.truffle.api.library=ALL-UNNAMED"

rotate-jahia-logs.sh &

echo "Start catalina... : /usr/local/tomcat/bin/catalina.sh $OPT"
exec /usr/local/tomcat/bin/catalina.sh $OPT
