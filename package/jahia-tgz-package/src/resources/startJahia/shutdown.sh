#!/bin/sh
echo -- JAHIA Edition 4 Shutdown script ----------------
if [ -f "../jre/jre/bin/java" ] ; then
  export JAVA_HOME=$PWD/../jre/jre
  echo Detected JRE, setting JAVA_HOME to $JAVA_HOME and PATH to $PATH
fi
PATH=$JAVA_HOME/bin:$PATH
export PATH
#cd ../hsqldb/demo
#./shutdown.sh
#cd ../..
if [ -f "../tomcat/bin/bootstrap.jar" ] ; then
  cd ../tomcat
    if [ -f "./bin/shutdown.sh" ] ; then
        ./bin/shutdown.sh
    fi
    rm -f webapps/jahia/.lock
    cd ../bin
    echo -- done. -------------------------------------
else
    echo Please run this script from the bin directory
fi
