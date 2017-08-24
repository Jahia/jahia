#!/bin/sh
cp -r * /usr/local/apache-tomcat-8.0.28-7.2/webapps/ROOT/engines/jahia-anthracite
sass -w edit_en.scss edit_fr.scss manager_en.scss manager_fr.scss
