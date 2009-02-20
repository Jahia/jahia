#!/bin/sh
# The syntax for this command is the following :
# mvndeploy GROUP_ID ARTIFACT_ID VERSION_ID PATH_TO_JAR_TO_DEPLOY
# 
# For this deploy to work, you will need the following 
# 1. Setup a public/private key with puttygen and convert it to OpenSSH format
# 2. Add the following information in your ${USER_HOME}/.m2/settings.xml file : 
#
#  <servers>
#    <server>
#      <id>jahiaRepository</id>
#      <username>shuber</username>
#      <privateKey>C:/putty/maven.jahia.org.private</privateKey> <!-- not needed if using pageant -->
#    </server>
#  </servers>
#
mvn deploy:deploy-file -DgroupId=$1 -DartifactId=$2 -Dversion=$3 -Dpackaging=jar -Dfile=$4 -DrepositoryId=jahiaRepository -Durl=scp://maven.jahia.org/var/www/vhosts/maven.jahia.org/html/maven2
