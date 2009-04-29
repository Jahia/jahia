<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html" language="java" %>
<%@page import ="java.util.*,java.io.*,org.jahia.bin.Jahia"%>
<%
  String jahiaPropFile = application.getRealPath("/WEB-INF/etc/config/jahia.properties");
  Properties jahiaProps = new Properties();
  try {
    jahiaProps.load(new FileInputStream(jahiaPropFile));
    response.setHeader("jahia-version", jahiaProps.getProperty("release"));
  } catch (FileNotFoundException fnfe) {
    response.sendError(500, "Error while loading Jahia properties file");
  }
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<body>
  <!--
    Jahia-Release : <%=jahiaProps.getProperty("release")%>
    Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%>
  -->
</body>
</html>