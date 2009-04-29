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
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String engineUrl = (String) engineMap.get("engineUrl");
    final String theScreen = (String) engineMap.get("screen");
    final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");

    final boolean showEditMenu = false;
    request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));
%>

<script type="text/javascript">
    window.onunload = closeTheWindow;
</script>

<h3 class="restore">
    <fmt:message key="org.jahia.engines.restorelivecontainer.RestoreLiveContainer_Engine.restore.label"/>
    <span>
        [ID = <%=theContainer.getID()%>]
    </span>
</h3>

<!-- Buttons -->
<jsp:include page="../buttons.jsp" flush="true"/>
<!-- End Buttons -->

<div class="clearing">&nbsp;</div>

<h5>
    <fmt:message key="org.jahia.engines.restorelivecontainer.RestoreLiveContainer_Engine.restoretext.label"/>
</h5>

<div class="clearing">&nbsp;</div>



