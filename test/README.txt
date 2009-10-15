For executing unit test where maven take care of your tomcat just activate the profile : testJahia

You need also to add these lien to your active profile inside your settings.xml :

<jahia.test.url>http://localhost:8080/jahia</jahia.test.url>

Example : mvn -P testJahia install

Previous example will execute all unit tests after doing a configure on your platform (deploy as to be done previously)



Now you can also execute one jmeter test on your running tomcat

#####################################################

If your on windows this will run only under CYGWIN

#####################################################

First you need to define where your jmeter is installed on your platform inside your settings.xml :

<jahia.test.jmeter.path>/home/rincevent/tools/jakarta-jmeter-2.3.4</jahia.test.jmeter.path>

You will also need to update the pom.xml for now with your database connections (Must be MYSQL) :

<db-ip>127.0.0.1</db-ip>
<db-login>jahia</db-login>
<db-pwd>jahia</db-pwd>
<db-name>jmeterdata</db-name>

(If you plan to use the default value, just create a database named jmeterdata).

The maven script take care of copying all the files found inside src/main/scripts/jmeter to your jmeter installation under jmeter_path/bin/testPlan

The groovy scripts expect to find the jmeter results inside jmeter_path/bin/results.

The default behavior is to import the ACME site demo inside your jahia

Ask jahia the sitemap of the site

Copy the sitemap to jmeter_path/bin/testPlan/pageurls.txt

And then start jmeter.

To execute the jmeter test just run : mvn -P testJahia,jmeterTest install