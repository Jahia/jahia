if [ "$1" = "" ] ; then
  MAVEN_SITE_PATH="/tmp"
else
  MAVEN_SITE_PATH="$1"
fi
mvn site
mvn -Djahia.site.path=$MAVEN_SITE_PATH site:deploy