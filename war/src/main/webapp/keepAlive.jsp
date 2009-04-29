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
<%
response.setHeader("Cache-Control","no-cache, no-store, must-revalidate, private");
response.setHeader("Pragma","no-cache");
response.setDateHeader ("Expires", 295122600000L);
if (request.getSession(false) == null || !request.isRequestedSessionIdValid()) {
    request.getSession(true);
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
} else {
    request.getSession().getId();
	response.setStatus(HttpServletResponse.SC_OK);
}%>