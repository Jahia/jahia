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
<%@ page import="java.util.*"%>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine"%>
<%@ page language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%!
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger("jsp.jahia.engines.selectpage.close");
%>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String operation = (String)engineMap.get(SelectPage_Engine.OPERATION);

    //added to support the page selected's parameter return for content picker usage
    final String pageSelected = request.getParameter("sourcePageID");
    final String callback = (String)engineMap.get("cond");
    
    logger.debug("condition="+callback);
%>

<script type="text/javascript">
    window.close();
    <c:set var="ctxId" value="${param['contextId']}"/>
    if ('${ctxId}'.length > 0 && window.opener.document.getElementById('${ctxId}')) {
    	window.opener.document.getElementById('${ctxId}').value = '<%=pageSelected%>';
    } else {
        window.opener.handleActionChanges ("edit&shouldSetPageLinkID=true&operation=<%=operation%>&destpageid=<%=pageSelected%>&pageSelected=<%=pageSelected%>&callback=<%=callback%>", "<%=pageSelected%>");
    }
</script>

