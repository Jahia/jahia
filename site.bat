@echo off
set MAVEN_SITE_PATH=%1
set USE_MAVEN_REPO=-Dmaven.repo.local=%2
if "%1" == "" goto setMavenSite

:checkMavenRepository
if "%2" == "" goto setMavenRepository
goto callMaven

:setMavenSite
set MAVEN_SITE_PATH=d:\tmp
goto checkMavenRepository

:setMavenRepository
set USE_MAVEN_REPO=


:callMaven
echo Maven site path : %MAVEN_SITE_PATH%
echo Use maven repository : %USE_MAVEN_REPO%

mvn clean -DgenerateReports=false site:site -Djahia.site.path=%MAVEN_SITE_PATH% site:deploy %USE_MAVEN_REPO%