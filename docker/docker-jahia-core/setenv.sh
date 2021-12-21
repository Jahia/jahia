#!/bin/sh

# GC setup
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+UseParallelGC -XX:SurvivorRatio=8"

# JMX setup
export CATALINA_OPTS="${CATALINA_OPTS} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=7199 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

# Log/debug info
export CATALINA_OPTS="${CATALINA_OPTS} -Xlog:gc::time,uptime,level,pid,tid,tags -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintConcurrentLocks"

# Memory settings
export CATALINA_OPTS="${CATALINA_OPTS} -XX:+UseContainerSupport -XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE}"

# CVE-2021-44228
export CATALINA_OPTS="${CATALINA_OPTS} -Dlog4j2.formatMsgNoLookups=true"
