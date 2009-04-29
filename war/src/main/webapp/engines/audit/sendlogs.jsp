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
<%@ page language = "java" %>
<%@ page import   = "org.jahia.params.*, java.util.*, javax.servlet.http.*" %>

<%
    Map engineMap   = (Map) request.getAttribute( "org.jahia.engines.EngineHashMap" );
    List logData        = (List) engineMap.get( "logData" );

    response.setHeader("Content-disposition", "filename=jahia_log.txt");
    response.setHeader("Content-type", "application/octetstream");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");
    %>

<%
    for(int i=0; i < logData.size(); i++) {
        Map logRecord = (Map) logData.get(i);
%> <%=logRecord.get("timeStr")%>  <%=logRecord.get("username")%>  <%=logRecord.get("operation")%> 
<% } %>

