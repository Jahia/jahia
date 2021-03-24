#!/bin/bash

if [ ! -f "/usr/local/tomcat/conf/configured" ]; then
    echo "Initial container startup, configuring Jahia..."

    OVERWRITEDB="if-necessary"

    if [ -f "/data/digital-factory-data/info/version.properties" ] || [ -d "/data/digital-factory-data/bundles-deployed" ]; then
        echo "Previous installation detected. Do not override db and existing data."
        PREVIOUS_INSTALL="true"
        OVERWRITEDB="false"
    else
        PREVIOUS_INSTALL="false"
    fi

    if [ -f "/data/env" ]; then
        source /data/env
    else
        echo "Storing environment to prevent changes"
        echo "DB_VENDOR=${DB_VENDOR}" >> /data/env
        echo "DB_VENDOR=${DB_VENDOR}" >> /data/env
        echo "DB_HOST=${DB_HOST}" >> /data/env
        echo "DB_NAME=${DB_NAME}" >> /data/env
        echo "DB_USER=${DB_USER}" >> /data/env
        echo "DB_PASS=${DB_PASS}" >> /data/env
        echo "DS_IN_DB=${DS_IN_DB}" >> /data/env
        echo "DS_PATH=${DS_PATH}" >> /data/env
    fi

    echo "Updating digital-factory-data..."
    cp -a /usr/local/tomcat/digital-factory-data/* /data/digital-factory-data/

    echo "Update /usr/local/tomcat/conf/server.xml..."
    sed -i '/<!-- Access log processes all example./i \\t<!-- Remote IP Valve -->\n \t<Valve className="org.apache.catalina.valves.RemoteIpValve" protocolHeader="X-Forwarded-Proto" />\n' /usr/local/tomcat/conf/server.xml
    sed -i 's/pattern="%h /pattern="%{org.apache.catalina.AccessLog.RemoteAddr}r /' /usr/local/tomcat/conf/server.xml
    sed -i 's/prefix="localhost_access_log"/prefix="access_log" rotatable="true" maxDays="'$LOG_MAX_DAYS'"/g' /usr/local/tomcat/conf/server.xml
    sed -i 's/^\([^#].*\.maxDays\s*=\s*\).*$/\1'$LOG_MAX_DAYS'/' /usr/local/tomcat/conf/logging.properties
    sed -i '/name="ROLL"/,+2 s/debug/warn/' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/etc/config/log4j.xml

    echo "Check database..."
    case "$DB_VENDOR" in
        "mariadb")
            DB_PORT="3306"
            DB_URL="jdbc:mariadb://${DB_HOST}/${DB_NAME}?useUnicode=true&amp;characterEncoding=UTF-8&amp;useServerPrepStmts=false&amp;useSSL=false"
            alive.sh ${DB_HOST} 3306
            ;;
        "postgresql")
            DB_PORT="5432"
            DB_URL="jdbc:postgresql://${DB_HOST}/${DB_NAME}"
            alive.sh ${DB_HOST} 5432
            ;;
        "derby_embedded")
            DB_URL="jdbc:derby:directory:/data/jahiadb;create=true"
            DS_IN_DB=false
            ;;
    esac

    if [ "${JAHIA_LICENSE}" != "" ]; then
      echo "decoding license"
      echo "${JAHIA_LICENSE}" | base64 --decode > /data/license.xml
      JAHIA_LICENSE_OPTS="-Djahia.configure.licenseFile=/data/license.xml"
    else
      echo "No license provided via environment variable"    
    fi

    echo "Configure jahia..."

    echo "/opt/apache-maven-${MAVEN_VER}/bin/mvn ${JAHIA_PLUGIN}:configure \
    -Djahia.deploy.targetServerType="tomcat" \
    -Djahia.deploy.targetServerDirectory="/usr/local/tomcat" \
    -Djahia.deploy.dataDir="/data/digital-factory-data" \
    -Djahia.configure.externalizedTargetPath="/usr/local/tomcat/conf/digital-factory-config" \
    -Djahia.configure.databaseType="${DB_VENDOR}" \
    -Djahia.configure.databaseUrl="${DB_URL}" \
    -Djahia.configure.databaseUsername="${DB_USER}" \
    -Djahia.configure.databasePassword=xxxxx \
    -Djahia.configure.storeFilesInDB="${DS_IN_DB}" \
    -Djahia.configure.fileDataStorePath="${DS_PATH}" \
    -Djahia.configure.jahiaRootPassword=xxxxx \
    -Djahia.configure.processingServer="${PROCESSING_SERVER}" \
    -Djahia.configure.operatingMode="${OPERATING_MODE}" \
    -Djahia.configure.deleteFiles="false" \
    -Djahia.configure.overwritedb="${OVERWRITEDB}" \
    -Djahia.configure.jahiaProperties="{mvnPath:\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",svnPath:\"/usr/bin/svn\",gitPath:\"/usr/bin/git\",karaf.remoteShell.host:\"0.0.0.0\"}" \
    $JAHIA_CONFIGURE_OPTS $JAHIA_LICENSE_OPTS -Pconfiguration"

    /opt/apache-maven-${MAVEN_VER}/bin/mvn ${JAHIA_PLUGIN}:configure \
    -Djahia.deploy.targetServerType="tomcat" \
    -Djahia.deploy.targetServerDirectory="/usr/local/tomcat" \
    -Djahia.deploy.dataDir="/data/digital-factory-data" \
    -Djahia.configure.externalizedTargetPath="/usr/local/tomcat/conf/digital-factory-config" \
    -Djahia.configure.databaseType="${DB_VENDOR}" \
    -Djahia.configure.databaseUrl="${DB_URL}" \
    -Djahia.configure.databaseUsername="${DB_USER}" \
    -Djahia.configure.databasePassword="${DB_PASS}" \
    -Djahia.configure.storeFilesInDB="${DS_IN_DB}" \
    -Djahia.configure.fileDataStorePath="${DS_PATH}" \
    -Djahia.configure.jahiaRootPassword="${SUPER_USER_PASSWORD}" \
    -Djahia.configure.processingServer="${PROCESSING_SERVER}" \
    -Djahia.configure.operatingMode="${OPERATING_MODE}" \
    -Djahia.configure.deleteFiles="false" \
    -Djahia.configure.overwritedb="${OVERWRITEDB}" \
    -Djahia.configure.jahiaProperties="{mvnPath:\"/opt/apache-maven-${MAVEN_VER}/bin/mvn\",svnPath:\"/usr/bin/svn\",gitPath:\"/usr/bin/git\",karaf.remoteShell.host:\"0.0.0.0\"}" \
    $JAHIA_CONFIGURE_OPTS $JAHIA_LICENSE_OPTS -Pconfiguration

    if [ -f "/data/digital-factory-data/info/passwd" ] && [ "`cat "/data/digital-factory-data/info/passwd"`" != "`echo -n "$SUPER_USER_PASSWORD" | sha256sum`" ]; then
        echo "Update root's password..."
        echo "$SUPER_USER_PASSWORD" > $FACTORY_DATA/root.pwd
    fi

    if [ "${EXECUTE_PROVISIONING_SCRIPT}" != "" ]; then
      echo " - include: ${EXECUTE_PROVISIONING_SCRIPT}" > $FACTORY_DATA/patches/provisioning/999-docker-provisioning.yaml
    fi

    echo -n "$SUPER_USER_PASSWORD" | sha256sum > /data/digital-factory-data/info/passwd

    touch "/usr/local/tomcat/conf/configured"
fi

if [ "$RESTORE_MODULE_STATES" == "true" ]; then
    echo " -- Restore module states have been asked"
    touch "$FACTORY_DATA/[persisted-bundles].dorestore"
fi

if [ "$RESTORE_PERSISTED_CONFIGURATION" == "true" ]; then
    echo " -- Restore OSGi configuration have been asked"
    touch "$FACTORY_DATA/[persisted-configurations].dorestore"
fi

if [ "$JPDA" == "true" ]; then
  OPT="jpda run"
else
  OPT="run"
fi

echo "Start catalina... : /usr/local/tomcat/bin/catalina.sh $OPT"
exec /usr/local/tomcat/bin/catalina.sh $OPT
