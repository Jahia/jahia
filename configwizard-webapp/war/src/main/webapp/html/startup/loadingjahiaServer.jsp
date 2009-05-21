<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
