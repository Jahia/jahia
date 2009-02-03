#!/bin/sh
echo -- JAHIA Edition 4 Startup Script --------------------
echo Verifying environment...
echo Current directory is $PWD
BINDIR=$PWD
export BINDIR
unset JAHIA_JIKES_HOME
if [ -f "../jre/jre/bin/java" ] ; then
  export JAVA_HOME=$PWD/../jre/jre
  echo Detected JRE, setting JAVA_HOME to $JAVA_HOME and PATH to $PATH
fi
if [ -z "$JAVA_HOME" ] ; then
  echo Please set your JAVA_HOME variable to the location where your JDK is installed before running JAHIA Edition 4...
  exit 0
else
  if [ -f "$JAVA_HOME/lib/tools.jar" ] ; then
    echo JDK found.
  else
    if [ -f "../jikes/bin/jikes" ] ; then
      export PATH=../jikes/bin:$PATH
      export JAHIA_JIKES_HOME=../jikes
      echo Jikes compiler detected. PATH=$PATH, JAHIA_JIKES_HOME=$JAHIA_JIKES_HOME
    else
      echo Error : JAVA_HOME does not seem to contain a valid JDK installation
      exit 0
    fi
  fi
fi
PATH=$JAVA_HOME/bin:$PATH
export PATH
if [ -z "$CATALINA_HOME" ]; then
  echo Catalina environment seems clean...
else
  echo CATALINA_HOME environment variable must NOT be set !
  exit 0
fi
if [ -z "$TOMCAT_HOME" ]; then
  echo Tomcat environment seems clean...
else
  echo TOMCAT_HOME environment variable must NOT be set !
  exit 0
fi
echo Starting JAHIA Edition Database and Web Server...
if [ -z "$JAHIA_JIKES_HOME" ]; then
  ./bin/catalina.sh start
else
  ./bin/catalina-jikes.sh start
fi
echo -- done. -------------------------------------
