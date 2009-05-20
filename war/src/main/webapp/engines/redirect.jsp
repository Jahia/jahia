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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.engines.*" %>
<%@ page import="java.util.*" %>
<%
  final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
	String redirectUrl = (String)engineMap.get(JahiaEngine.ENGINE_REDIRECT_URL);
%>

<script language="JavaScript">
<!--

var sURL = '<%=redirectUrl%>';


function redirect()
{
    //  This version of the refresh function will cause a new
    //  entry in the visitor's history.
    window.location.href = sURL;
}

redirect();
//-->
</script>
