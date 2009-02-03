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
#cd ../hsqldb/demo
#./runServer
#cd ../..
cd ../tomcat
if [ -z "$JAHIA_JIKES_HOME" ]; then
  ./bin/catalina.sh start
else
  ./bin/catalina-jikes.sh start
fi
cd ../bin
#echo Started. Waiting for browser startup...
#cd ../tomcat/webapps/jahia
##java -classpath $CLASSPATH:./:./WEB-INF/lib/jahia-5.0.4_r21577.jar:../../shared/lib/log4j-1.2.15.jar org.jahia.init.TomcatWait http://localhost:8080/jahia/html/startup/startjahia.html
#java -classpath "$CLASSPATH:./:./WEB-INF/lib/jahia-5.0.4_r21577.jar:./WEB-INF/lib/log4j-1.2.15.jar" org.jahia.init.TomcatWait
#cd ../../../bin
#MOZILLAPATH=`which mozilla`
#if [ -x $MOZILLAPATH ] ; then
#  echo Mozilla found at $MOZILLAPATH, starting up...
#  $MOZILLAPATH http://localhost:8080/jahia/html/startup/loadingjahia.html &
#else
#  echo Mozilla not found. Trying Netscape... Note: Netscape 4.x will cause render problems.
#  NETSCAPEPATH=`which netscape`
#  if [ -x $NETSCAPEPATH ] ; then
#    echo Netscape found at $NETSCAPEPATH, starting up...
#    $NETSCAPEPATH http://localhost:8080/jahia/html/startup/loadingjahia.html &
#  else
#    echo Netscape not found. Please point your browser to the following URL : http://localhost:8080/jahia/Jahia
#  fi
#fi

echo -- done. -------------------------------------
