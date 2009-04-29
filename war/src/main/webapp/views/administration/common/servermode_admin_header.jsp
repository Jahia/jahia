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
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@page language = "java" %>
<%@page import = "java.util.*"%>

<%@include file="/views/administration/common/taglibs.jsp" %>
<jsp:useBean id="URL" class="java.lang.String"   	scope="request"/>


<div id="topTitle">
<h1>Jahia</h1>
<% String titleKey = request.getAttribute("dialogTitle") != null ? request.getAttribute("dialogTitle").toString() : "org.jahia.admin.serverSettings.label"; %>
<h2 class="edit"><fmt:message key="<%=titleKey %>"/></h2>
</div>