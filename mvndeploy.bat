@echo off
rem The syntax for this command is the following :
rem mvndeploy GROUP_ID ARTIFACT_ID VERSION_ID PATH_TO_JAR_TO_DEPLOY
rem 
rem For this deploy to work, you will need the following 
rem 1. Setup a public/private key with puttygen and convert it to OpenSSH format
rem 2. Add the following information in your ${USER_HOME}/.m2/settings.xml file : 
rem
rem  <servers>
rem    <server>
rem      <id>jahiaRepository</id>
rem      <username>shuber</username>
rem      <privateKey>C:/putty/maven.jahia.org.private</privateKey> <!-- not needed if using pageant -->
rem    </server>
rem  </servers>
rem
echo on
mvn deploy:deploy-file -DgroupId=%1 -DartifactId=%2 -Dversion=%3 -Dpackaging=jar -Dfile=%4 -DrepositoryId=jahiaRepository -Durl=scp://maven.jahia.org/var/www/vhosts/maven.jahia.org/html/maven2
