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
<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper" %>

<%@include file="/views/engines/common/taglibs.jsp" %>
<%
	String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
	String engineView = (String)request.getAttribute("engineView");
	PagesVersioningViewHelper pagesVersViewHelper = 
		(PagesVersioningViewHelper)request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

	Map engineMap = (Map)request.getAttribute("jahia_session_engineMap");
	String theScreen = (String)engineMap.get("screen");

%>
<%@include file="common-javascript.inc" %>
<div class="menuwrapper">
<%@ include file="../../../../engines/tools.inc" %>
<div class="content">
<div id="editor" class="mainPanel">
<h4 class="versioningIcon">
    <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
</h4>
<div class="clearing">&nbsp;</div>
<html:messages id="currentMessage" message="true">
  <ul>
    <li><bean:write name="currentMessage" /></li>
  </ul>
</html:messages>
</div>
</div>

<div class="clearing">&nbsp;</div>
</div>