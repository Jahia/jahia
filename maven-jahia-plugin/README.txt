==============================================================================
 README FOR CONFIGURING THE JAHIA:CONFIGURE MAVEN GOAL
==============================================================================

The configure goal contains many features to help you automatically configure your jahia instance
without going through the configuration wizard in Jahia. simply modify your pom.xml or settings.xml with the following properties

1. To start you jahia without having to create a virtual site, simply add something like

<siteImportLocation>
	<param>/home/bamboo/bin/siteImport_myCommunity.zip</param>
	<param>/home/bamboo/bin/siteImport_testSite.zip</param>
</siteImportLocation>

but this only works in your pom.xml file so all in all you should add 

<plugin>
                <groupId>org.jahia.server</groupId>
                <artifactId>maven-jahia-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <configuration>
                    <siteImportLocation>
                     <param>/home/bamboo/bin/siteImport_myCommunity.zip</param>
                     <param>/home/bamboo/bin/siteImport_testSite.zip</param>
                    </siteImportLocation>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>configure</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

to create as many virtual sites as you want

2. To deploy in a clustered environment simply add something like

<jahia.configure.cluster_activated>true</jahia.configure.cluster_activated>

<jahia.configure.processingServer>true</jahia.configure.processingServer> <!-- in the case the node you are deploying to is the processing one -->

<jahia.configure.overwritedb>true</jahia.configure.overwritedb> <!-- in case you would like not to  override the database if you have multiple nodes to deploy to -->

<jahia.configure.cluster_node_serverId>qa-j3</jahia.configure.cluster_node_serverId> <!-- the name of your node  -->

<jahia.configure.localIp>10.8.37.243</jahia.configure.localIp> <!-- the ip of your node -->
<jahia.configure.clusterNodes>
    <!-- here add as many nodes as you want, but be careful as you may encounter some licence problems-->
	<param>10.8.37.243</param> 
    <param>10.8.37.244</param>
</jahia.configure.clusterNodes>

in the configure section of your plugin

3. By default, the root user/pass in jahia is root/root1234
If you want to change it, simply add
<jahia.configure.jahiaRootPassword>rootroot</jahia.configure.jahiaRootPassword> 


Any property present in the jahia.properties file can be parametrized. If you do not precise any particular
value, it will get the default value when configuring jahia by hand

 
