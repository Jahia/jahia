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
<%@ page import="org.jahia.bin.JahiaConfigurationWizard" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="JahiaMessageResources" />
<%
    String step = "";
    String steps = JahiaConfigurationWizard.getSteps();
    String op = request.getParameter("sub");
    if (op != null) {
        if (op.equals("add")) {
            step = step + ".1";
        } else if (op.equals("processadd") || op.equals("displaycreateadmin") || op.equals("prepareimport")) {
            step = step + ".1";
        } else if (op.equals("processcreateadmin") || op.equals("processimport")) {
            step = step + ".2";
        } else if (op.equals("processtemplatesetchoice")) {
            if (request.getAttribute("siteTitle")==null) {
                step = step + ".2";
            } else {
                step = step + ".3";
            }
        }
    }
%>

<div id="topTitle">
    <h1>
        Jahia
    </h1>
    <h2 class="edit"><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/></h2>
    <span class="step">
        <% if (step != null && step.length() > 0) { %>
            <fmt:message key="org.jahia.step.label"/>&nbsp;<%=step%> / <%=steps%>
        <% } else { %>
            &nbsp;
        <% } %>
    </span>
</div>
