#!/bin/bash

if [ ! -f "${CATALINA_HOME}/conf/configured" ]; then
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
    cp -a ${CATALINA_HOME}/digital-factory-data/* ${DATA_FOLDER}/

    echo "Updating ${CATALINA_HOME}/conf/server.xml and logging.properties..."
    sed -i "s|#LOGS_FOLDER#|$LOGS_FOLDER|g" ${CATALINA_HOME}/conf/server.xml ${CATALINA_HOME}/conf/logging.properties

    if [ "$SSL_ENABLED" == "true" ]; then
      if [ ! -d ${CATALINA_HOME}/conf/ssl ]; then
        mkdir -p ${CATALINA_HOME}/conf/ssl
        openssl req -x509 -newkey rsa:4096 -keyout ${CATALINA_HOME}/conf/ssl/localhost-rsa-key.pem -out ${CATALINA_HOME}/conf/ssl/localhost-rsa-cert.pem -days 36500 -subj "/CN=localhost" -passout env:SSL_CERTIFICATE_PASSWD
      fi

      sed -i '/#SSL_DISABLED#/d' ${CATALINA_HOME}/conf/server.xml
      sed -i "s/#SSL_CERTIFICATE_PASSWD#/${SSL_CERTIFICATE_PASSWD}/" ${CATALINA_HOME}/conf/server.xml
    fi

    echo "Update log4j..."
    sed -i 's/ref="RollingJahia/ref="Jahia/' ${CATALINA_HOME}/webapps/ROOT/WEB-INF/etc/config/log4j2.xml

    sed -i "s|#LOGS_FOLDER#|$LOGS_FOLDER|g;s|#LOG_MAX_DAYS#|$LOG_MAX_DAYS|g;s|#LOG_MAX_SIZE#|$LOG_MAX_SIZE|g" ${CATALINA_HOME}/conf/jahia_logrotate

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

    if [ "${JAHIA_PROPERTIES}" != "" ]; then
      JAHIA_PROPERTIES="${JAHIA_PROPERTIES},\"mvnPath\":\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",\"svnPath\":\"/usr/bin/svn\",\"gitPath\":\"/usr/bin/git\",\"karaf.remoteShell.host\":\"0.0.0.0\""
    else
      JAHIA_PROPERTIES="\"mvnPath\":\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",\"svnPath\":\"/usr/bin/svn\",\"gitPath\":\"/usr/bin/git\",\"karaf.remoteShell.host\":\"0.0.0.0\""
    fi


    # Create or overwrite install.properties file
    PROPERTIES_FILE="/tmp/install.properties"
    echo "# Generated install.properties file" > $PROPERTIES_FILE

    # Extract values from the command arguments, removing -Djahia.configure prefix
    echo "targetServerDirectory=${CATALINA_HOME}" >> $PROPERTIES_FILE
    echo "databaseType=${DB_VENDOR}" >> $PROPERTIES_FILE
    echo "jahiaVarDiskPath=${DATA_FOLDER}" >> $PROPERTIES_FILE
    echo "externalizedConfigTargetPath=${ETC_FOLDER}" >> $PROPERTIES_FILE
    echo "databaseUrl=${DB_URL}" >> $PROPERTIES_FILE
    echo "databaseUsername=${DB_USER}" >> $PROPERTIES_FILE
    echo "databasePassword=${DB_PASS}" >> $PROPERTIES_FILE
    echo "storeFilesInAWS=${DS_IN_AWS}" >> $PROPERTIES_FILE
    echo "storeFilesInDB=${DS_IN_DB}" >> $PROPERTIES_FILE
    echo "fileDataStorePath=${DS_PATH}" >> $PROPERTIES_FILE
    echo "jahiaRootPassword=${SUPER_USER_PASSWORD}" >> $PROPERTIES_FILE
    echo "processingServer=${PROCESSING_SERVER}" >> $PROPERTIES_FILE
    echo "operatingMode=${OPERATING_MODE}" >> $PROPERTIES_FILE
    echo "overwritedb=${OVERWRITEDB}" >> $PROPERTIES_FILE
    echo "jahiaProperties={${JAHIA_PROPERTIES}}" >> $PROPERTIES_FILE
    # Handle external opts
    echo "processing OPTS: ${JAHIA_CONFIGURE_OPTS}"
    echo ${JAHIA_CONFIGURE_OPTS} | sed -E 's/-Djahia\.[configure,data,deploy]+\.([^=]+)=/\n\1=/g'  >> $PROPERTIES_FILE

    if [ "${JAHIA_LICENSE}" != "" ]; then
      echo "decoding license"
      echo "${JAHIA_LICENSE}" | base64 --decode > ${DATA_FOLDER}/license.xml
     echo "licenseFile=${DATA_FOLDER}/license.xml" >> $PROPERTIES_FILE
    else
      echo "No license provided via environment variable"
    fi

    echo "Configuring Jahia with command:"
    echo "java -cp \"/opt/jahia/configurator.jar:${CATALINA_HOME}/lib/*\" org.jahia.configuration.ConfigureMain --configure ${PROPERTIES_FILE}"
    java -cp "/opt/jahia/configurator.jar:${CATALINA_HOME}/lib/*" org.jahia.configuration.ConfigureMain --configure ${PROPERTIES_FILE}

    echo "Backup: ${ETC_FOLDER}/jahia to ${DATA_FOLDER}/info/initial-config (useful for future migrating to new versions)"
    mkdir -p ${DATA_FOLDER}/info/initial-config
    cp ${ETC_FOLDER}/jahia/* ${DATA_FOLDER}/info/initial-config

    if [ -f "${DATA_FOLDER}/info/passwd" ] && [ "`cat "${DATA_FOLDER}/info/passwd"`" != "`echo -n "$SUPER_USER_PASSWORD" | sha256sum`" ]; then
        echo "Update root's password..."
        echo "${SUPER_USER_PASSWORD}" > ${DATA_FOLDER}/root.pwd
    fi

    if [ "${EXECUTE_PROVISIONING_SCRIPT}" != "" ]; then
      echo " - include: ${EXECUTE_PROVISIONING_SCRIPT}" > ${DATA_FOLDER}/patches/provisioning/999-docker-provisioning.yaml
    fi

    echo -n "${SUPER_USER_PASSWORD}" | sha256sum > ${DATA_FOLDER}/info/passwd

    touch "${CATALINA_HOME}/conf/configured"
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

echo "Start catalina... : ${CATALINA_HOME}/bin/catalina.sh $OPT"
exec ${CATALINA_HOME}/bin/catalina.sh $OPT
