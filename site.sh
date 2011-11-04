if [ "$1" = "" ] ; then
  MAVEN_SITE_PATH="/tmp"
else
  MAVEN_SITE_PATH="$1"
fi
if [ "$2" = "" ] ; then
  USE_MAVEN_REPO=""
else
  USE_MAVEN_REPO="-Dmaven.repo.local=$2"
fi
mvn clean $USE_MAVEN_REPO
mvn -DgenerateReports=false site:site $USE_MAVEN_REPO
mvn -Djahia.site.path=$MAVEN_SITE_PATH site:deploy $USE_MAVEN_REPO