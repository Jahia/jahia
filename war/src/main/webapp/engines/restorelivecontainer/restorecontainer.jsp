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



