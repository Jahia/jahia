<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
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
<%
    String step = "8";
    String steps = "10";
    String op = request.getParameter("sub");
    if (op != null) {
        if (op.equals("add")) {
            step = step + ".1";
        } else if (op.equals("processadd") || op.equals("displaycreateadmin")) {
            step = step + ".2";
        }else if (op.equals("prepareimport")){
        	step = step + ".1";
        	} else if (op.equals("processcreateadmin")) {
            step = step + ".3";
        }else if (op.equals("processimport")){
        	step = "9";
        	}
        	else if (op.equals("createsite") ){
        	step = "10";
        	}
         else if (op.equals("processtemplatesetchoice")) {
            if (request.getAttribute("siteTitle")==null) {
                step = step + ".3";
            } else {
                step = "9";
            }
        }
    }
%>

<div id="topTitle">
    <h1>
        Jahia
    </h1>
    <h2 class="edit"><internal:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/></h2>
    <span class="step">
        <% if (step != null && step.length() > 0) { %>
            <internal:message key="org.jahia.step.label"/>&nbsp;<%=step%> / <%=steps%>
        <% } else { %>
            &nbsp;
        <% } %>
    </span>
</div>
