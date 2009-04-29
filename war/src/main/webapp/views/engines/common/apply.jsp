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

<%!
    private String composeApplyJahiaWindowUrl(final String url, final String screen) {
        if (url != null) {
            String applyJahiaWindowUrl = new String(url);
            if (url.indexOf('?') > -1) {
                // url has at least one parameter
                applyJahiaWindowUrl = applyJahiaWindowUrl + "&";
            } else {
                // url hasn't parameters
                applyJahiaWindowUrl = applyJahiaWindowUrl + "?";
            }
            applyJahiaWindowUrl = applyJahiaWindowUrl + "screen=" + screen;
            return applyJahiaWindowUrl;
        }
        return null;
    }
%>

<%
    Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    String javaScriptPath = (String) engineMap.get("javaScriptPath");
    String url = (String) engineMap.get("engineUrl");
    String screen = (String) engineMap.get("screen");

%>

<html>
<head>

    <script language="javascript" src="<%=javaScriptPath%>">
    </script>
    <script language="javascript">
        applyJahiaWindow("<%=composeApplyJahiaWindowUrl(url, screen)%>");
    </script>

</head>
<body bgcolor="white">
</body>
</html>

