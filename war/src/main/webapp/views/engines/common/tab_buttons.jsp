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
<%@page language = "java"%>
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.params.*"%>
<%@page import = "org.jahia.views.engines.*"%>

<%@include file="/views/engines/common/taglibs.jsp" %>

<%
	Map engineMap 	= (Map)request.getAttribute("jahia_session_engineMap");
    Boolean adminAccess = (Boolean) engineMap.get( "adminAccess" );
    String theScreen = (String) engineMap.get( "screen" );
%>