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

<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.utils.JahiaTools" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
        boolean exists = (new File("webapps/ROOT/WEB-INF/etc/config/jahia.properties")).exists();
        if (exists) {
        %>
            <meta http-equiv="Refresh" content="10;url=/cms/">
        <%
            // File did not exist and was created
        } else {
        	%>
        	 <meta http-equiv="Refresh" content="10;url=/config/">
        	<%
            // File already exists
        }
%>	
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Loading Server...</title>
<link href="misc/startup.css" rel="stylesheet" type="text/css" />
</head>

<body>
<div id="page">
  <div id="content">
  	<h1 class="hide">Loading Jahia Server...</h1>
      <p><strong>Welcome to Jahia.</strong> </p>
      <p><strong>Loading Jahia Server...</strong></p><br />
		<img class="wait" src="img/wait.gif" alt="" />
  </div>
</div>
</body>
</html>
