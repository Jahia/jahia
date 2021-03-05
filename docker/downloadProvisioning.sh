#!/bin/bash

REPO_EXPR='addRepository:[ "]*([a-zA-Z0-9:./_-]*)@id=([a-zA-Z0-9:./_-]*)'
REPOS=`grep -E "^[^#].*${REPO_EXPR}" $1 | sed -E "s/.*${REPO_EXPR}.*/<id>\2<\\/id><url>\1<\\/url>/g"`

for i in $REPOS
do
  echo Adding repository $i to settings.xml ...
  sed -e "/repositories here -->/a \        <repository>${i//\//\\/}<\/repository>" settings.xml
done


MVN_URL_EXPR="mvn:([a-zA-Z0-9./_-]*)"
MVN_URLS=`grep -E "^[^#].*${MVN_URL_EXPR}" $1 | sed -E "s/.*${MVN_URL_EXPR}.*/\1/g;s/\//:/g"`
for i in $MVN_URLS
do
  echo Download $i from maven repository ...
  mvn -q -U -Pconfiguration dependency:get -Dtransitive=false -Dartifact=$i
done
