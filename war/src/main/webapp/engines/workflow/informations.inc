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
<!-- content. There is to much JAVA scriptlets. In the future this code -->
<!-- should be optimized. -->
<!-- Begin informations.inc -->
<tr>
    <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="text6" colspan="100"> <!-- W3C compliant 0 value doesn't work !-->
        &nbsp;&nbsp;&nbsp;
        <% if (activationTestResults.getErrors().size() == 0) { %>
            <internal:displayIcon src="workflowErrors" alt="No errors to show" />&nbsp;<b><fmt:message key="org.jahia.engines.shared.Page_Field.no.label"/></b>
        <% } else { %>
            <% if (treeJahiaSiteMap.shouldShowErrors(i)) { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=hideerror|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowErrorsActive" alt="Hide errors" /></a>
            <% } else { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=showerror|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowErrors" alt="Show errors" /></a>
            <% } %>
        <% } %>
        &nbsp;<b><fmt:message key="org.jahia.engines.shared.Page_Field.errors.label"/> (<%=activationTestResults.getErrors().size()%>)</b><br>
        <% if (treeJahiaSiteMap.shouldShowErrors(i)) { %>
            <ul>
            <% for (int j = 0; j < activationTestResults.getErrors().size(); j++) {
                Object testResultObject = (Object)activationTestResults.getErrors().get(j);
                displayActivationResult(testResultObject, jParams, contentObject, i, pageContext);
               }
            %> </ul> &nbsp; <br/><%
        } %>
        &nbsp;&nbsp;&nbsp;
        <% if (activationTestResults.getWarnings().size() == 0) { %>
            <internal:displayIcon src="workflowWarnings" alt="No warnings to show" />&nbsp;<b><fmt:message key="org.jahia.engines.shared.Page_Field.no.label"/></b>
        <% } else { %>
            <% if (treeJahiaSiteMap.shouldShowWarnings(i)) { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=hidewarning|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowWarningsActive" alt="Hide warnings" /></a>
            <% } else { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=showwarning|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowWarnings" alt="Show warnings" /></a>
            <% } %>
        <% } %>
        &nbsp;<b><fmt:message key="org.jahia.warning.label"/> (<%=activationTestResults.getWarnings().size()%>)</b><br>
        <% if (treeJahiaSiteMap.shouldShowWarnings(i)) { %>
            <ul>
            <% for (int j = 0; j < activationTestResults.getWarnings().size(); j++) {
                Object testResultObject = activationTestResults.getWarnings().get(j);
                displayActivationResult(testResultObject, jParams, contentObject, i, pageContext);
                }
            %> </ul> &nbsp; <br/><%
        } %>
        &nbsp;&nbsp;&nbsp;
        <internal:displayIcon src="workflowEvents" alt="No events to show" />
        &nbsp;<b><fmt:message key="org.jahia.engines.shared.Page_Field.events.label"/>&nbsp;:&nbsp;</b><fmt:message key="org.jahia.notImplemented.label"/><br>
    </td>
</tr>
<!-- End informations.inc -->