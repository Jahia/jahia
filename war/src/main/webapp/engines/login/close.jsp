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
<%@ page import="org.jahia.params.ParamBean" %>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String javaScriptPath = (String) engineMap.get("javaScriptPath");
    final String reloadUrl = (String) engineMap.get("engineUrl");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
%>

<script type="text/javascript" src="<%=javaScriptPath%>">
</script>
<script type="text/javascript">
    if (window.opener == null) {
        window.location.href = "<%=jParams.composePageUrl(jParams.getPageID())%>";
    } else {
        window.opener.location.href = "<%=reloadUrl%>";
        window.close();
    }
</script>
