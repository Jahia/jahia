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