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
<%@ page language="java" %>
<%@ page import="org.jahia.data.search.*" %>
<%@ page import="org.jahia.params.*" %>
<%@ page import="org.jahia.services.*" %>
<%@ page import="org.jahia.services.usermanager.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    Map<String, Object> engineMap   = (Map<String, Object>) request.getAttribute( "org.jahia.engines.EngineHashMap" );
    String engineUrl    = (String) engineMap.get( "engineUrl" );
    String theScreen    = (String) engineMap.get( "screen" );
    String javaScriptPath   = (String) engineMap.get( "javaScriptPath" );
    ParamBean jParams   = (ParamBean) request.getAttribute( "org.jahia.params.ParamBean" );
    String theURL       = (String) jParams.settings().getJahiaEnginesHttpPath();

    JahiaSearchResult searchResults = (JahiaSearchResult) engineMap.get( "searchResults" );
    String searchString	= (String) engineMap.get ("searchString");

%>

<html>
<head>
    <title><fmt:message key="org.jahia.engines.search.Search_Engine.searchResultsTitle.label"/></title>
    <script language="javascript" src="<%=javaScriptPath%>"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}<fmt:message key="org.jahia.stylesheet.css"/>" type="text/css">
</head>

<body class="text" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="window.focus(); MM_preloadImages('${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOn.button"/>','${pageContext.request.contextPath}<fmt:message key="org.jahia.okOn.button"/>');">
<table width="100%" height="63" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td width="126" height="63"><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.header.image"/>" width="126" height="63"></td>
        <td height="63" width="100%" background="${pageContext.request.contextPath}<fmt:message key="org.jahia.headerBg.image"/>">&nbsp;</td>
    </tr>
</table>
<br>
<p class="text">
&nbsp;&nbsp;&nbsp;<b><fmt:message key="org.jahia.engines.search.Search_Engine.searchResultsFor.label"/>[</b><%=searchString%><b>]</b>
</p>

<%  if ((searchResults != null) && (searchResults.results().size() != 0))
    {
        for (JahiaSearchHit thisHit : searchResults.results())
        {
            %>
            &nbsp;&nbsp;&nbsp;
           <a href="<%=thisHit.getPage().getUrl(jParams)%>"> <%=thisHit.getPage().getTitle()%></a>
           - <fmt:message key="org.jahia.engines.search.Search_Engine.score.label"/> : <%=thisHit.getScore()%><br>
           &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;   <%=thisHit.getTeaser()%><br><br>
<%      }
    } %>

</body>
</html>