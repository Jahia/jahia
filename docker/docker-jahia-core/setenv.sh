#!/bin/sh

# GC setup
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+UseG1GC -XX:+DisableExplicitGC -XX:+UseStringDeduplication -XX:MaxTenuringThreshold=7"
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+ParallelRefProcEnabled -XshowSettings:vm -XX:+UnlockDiagnosticVMOptions "
export CATALINA_OPTS="${CATALINA_OPTS} -XX:GuaranteedSafepointInterval=0 -XX:-UseBiasedLocking -XX:+UseCountedLoopSafepoints -XX:LoopStripMiningIter=100"
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+SafepointTimeout -XX:SafepointTimeoutDelay=1000"
# JMX setup
export CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7199 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

# Log/debug info
export CATALINA_OPTS="${CATALINA_OPTS} -Xlog:gc*,gc+ref=debug,gc+heap=debug,gc+age=trace:file=gc-%p-%t.log:tags,uptime,time,level:filecount=10,filesize=20m"
export CATALINA_OPTS="${CATALINA_OPTS} -Xlog:os+container=debug,pagesize=debug:file=os-container-pagesize-%p-%t.log:tags,uptime,time,level:filecount=10,filesize=20m"
export CATALINA_OPTS="${CATALINA_OPTS} -Xlog:safepoint*:file=safepoints-%p-%t.log:tags,uptime,time,level:filecount=10,filesize=20m"
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintConcurrentLocks"

# Memory settings
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+UseContainerSupport -XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE}"

# CVE-2021-44228
export CATALINA_OPTS="${CATALINA_OPTS} -Dlog4j2.formatMsgNoLookups=true"

#XML External Entity (XXE) Processing
export CATALINA_OPTS="${CATALINA_OPTS} -Djavax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema=com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"

# Prevent Karaf from intercepting sigterm
export CATALINA_OPTS="${CATALINA_OPTS} -Dkaraf.handle.sigterm=false"
