# mvn install -DsingleModule=true -DsingleModuleName=org.jahia.ajax.gwt.templates.entrypoint.general.live.Live
if [ "$1" = "" ] ; then
  MAVEN_GOAL="install"
else
  MAVEN_GOAL="$1"
fi
if [ "$2" = "" ] ; then
  MODULE_NAME=""
else
  MODULE_NAME="-DsingleModuleName=$2"
fi
echo "Maven goal  : $MAVEN_GOAL"
echo "Module name : $MODULE_NAME"
mvn $MAVEN_GOAL -DsingleModule=true $MODULE_NAME $3 $4 $5 $6
