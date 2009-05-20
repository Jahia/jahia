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