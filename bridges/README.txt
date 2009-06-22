
Quick guide on how to install the bridge into a Struts-based application.

1. Prepare the WAR file with the administration Prepare WAR tool and then download the modified WAR

2. De-compress the war into a directory, that we will call WEBAPP_DIR. You can decompress the WAR file using the
command line command : jar xvf WAR_FILE_NAME .
You should then delete or move the WAR_FILE_NAME outside of the WEBAPP_DIR directory.

3. Copy the jahia-bridges-*.jar from the Jahia's WEB-INF/lib to the Struts web application WEBAPP_DIR/WEB-INF/lib
directory. You should also copy the portals-bridges-struts-1.2.7-1.0.4.jar from the portlet bridges project
(http://portals.apache.org/bridges/download.html) into the WEBAPP_DIR/WEB-INF/lib directory.

4. Modify the WEBAPP_DIR/WEB-INF/web.xml file to replace the PortletServlet with the following class :

<servlet-class>org.jahia.portal.pluto.bridges.struts.PlutoStrutsPortletServlet</servlet-class>

5. Modify or create the WEBAPP_DIR/WEB-INF/portlet.xml file to use the Struts Portlet, like in the following example :

<portlet-app id="jpetstore"
    xmlns="http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd" version="1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd">
  <portlet id="JPetStorePortlet">
    <description>The JPetstore Portlet runs the JPetstore Struts application inside of a portlet. This is a good demo of how to develop Struts portlets.</description>
    <portlet-name>JPetstorePortlet</portlet-name>
    <display-name>JPetstore Portlet</display-name>
    <portlet-class>org.jahia.portal.pluto.bridges.struts.PlutoStrutsPortlet</portlet-class>
    <init-param>
      <name>ServletContextProvider</name>
      <value>org.jahia.portal.pluto.bridges.struts.PlutoServletContextProvider</value>
    </init-param>
    <init-param>
      <name>PortletScopeStrutsSession</name>
      <value>true</value>
    </init-param>
    <init-param>
      <name>ViewPage</name>
      <value>/index.shtml</value>
    </init-param>
    <init-param>
      <name>HelpPage</name>
      <value>/help.shtml</value>
    </init-param>
    <expiration-cache>-1</expiration-cache>
    <supports>
      <mime-type>text/html</mime-type>
      <portlet-mode>VIEW</portlet-mode>
      <portlet-mode>HELP</portlet-mode>
    </supports>
    <portlet-info>
      <title>JPetstore</title>
      <keywords>Struts,pet,petstore,store,jpetstore,demo,bridge</keywords>
    </portlet-info>
  </portlet>
</portlet-app>

6. Re-create the WAR file for your project by launching for example the following command line : jar cvf WAR_FILE_NAME .

7. You can now deploy the WAR_FILE_NAME to your application server and Jahia should detect it and make it available
to use on your web pages.

Tips & tricks :

If you have deployment problems, such as XML validation issues, please check that your web app doesn't include an XML
parser implementation. As Jahia already provides one, you should remove the one from your web application.