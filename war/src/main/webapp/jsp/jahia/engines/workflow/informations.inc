<%--
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.
--%>
<!-- content. There is to much JAVA scriptlets. In the future this code -->
<!-- should be optimized. -->
<!-- Begin informations.inc -->
<tr>
    <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
    <td class="text6" colspan="100"> <!-- W3C compliant 0 value doesn't work !-->
        &nbsp;&nbsp;&nbsp;
        <% if (activationTestResults.getErrors().size() == 0) { %>
            <internal:displayIcon src="workflowErrors" alt="No errors to show" />&nbsp;<b><internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.no.label"/></b>
        <% } else { %>
            <% if (treeJahiaSiteMap.shouldShowErrors(i)) { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=hideerror|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowErrorsActive" alt="Hide errors" /></a>
            <% } else { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=showerror|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowErrors" alt="Show errors" /></a>
            <% } %>
        <% } %>
        &nbsp;<b><internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.errors.label"/> (<%=activationTestResults.getErrors().size()%>)</b><br>
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
            <internal:displayIcon src="workflowWarnings" alt="No warnings to show" />&nbsp;<b><internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.no.label"/></b>
        <% } else { %>
            <% if (treeJahiaSiteMap.shouldShowWarnings(i)) { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=hidewarning|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowWarningsActive" alt="Hide warnings" /></a>
            <% } else { %>
                <a href="javascript:handleActionChanges('<%=screen%>&sitemap=showwarning|<%=i%>#<%=i%>')">
                    <internal:displayIcon src="workflowWarnings" alt="Show warnings" /></a>
            <% } %>
        <% } %>
        &nbsp;<b><internal:engineResourceBundle resourceName="org.jahia.warning.label"/> (<%=activationTestResults.getWarnings().size()%>)</b><br>
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
        &nbsp;<b><internal:engineResourceBundle resourceName="org.jahia.engines.shared.Page_Field.events.label"/>&nbsp;:&nbsp;</b><internal:engineResourceBundle resourceName="org.jahia.notImplemented.label"/><br>
    </td>
</tr>
<!-- End informations.inc -->