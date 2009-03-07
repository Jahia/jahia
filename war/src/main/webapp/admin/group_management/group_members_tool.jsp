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

<%@page language = "java" %>
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@page import = "org.jahia.services.pages.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="groupMessage" class="java.lang.String" scope="session"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<%
    String userSearch = (String)request.getAttribute("userSearch");
int stretcherToOpen   = 1;
%>

<script language="javascript" src="<%=request.getContextPath()%>/javascript/selectbox.js"></script>
<script language="javascript">
    window.onunload = closeEngineWin;
</script>
<script language="javascript">
function handleKey(e)
{
    pasteSelection();
}

function handleKeyCode(code)
{
}

function submitForm(action)
{
    document.mainForm.action = '<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=groupmembers&subaction=")%>' + action;
    document.mainForm.method = "post";
    document.mainForm.submit();
}

function pasteSelection()
{
    if (window.opener.closed) {
        //alert('ACL entries closed');
    } else {
        for (i = 0; i < document.mainForm.selectedUsers.length; i++) {
            if ((document.mainForm.selectedUsers.options[i].selected) &&
                (document.mainForm.selectedUsers.options[i].value != "null")) {
                window.opener.addOptions(document.mainForm.selectedUsers.options[i].text,
                                         document.mainForm.selectedUsers.options[i].value);
                document.mainForm.selectedUsers.options[i].selected = false;
            }
        }
        window.opener.addOptionsBalance();
    }
}

function pasteSelectionClose()
{
    pasteSelection();
    window.close();
}

</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="org.jahia.admin.users.GroupMembersTool.groupMembersAddition.label"/></h2>
</div>
<div id="main">
<!-- -->
<!-- Display group futures members -->
<table class="text" border="0" align="center" width="100%">
    <tr>
        <td align="right">
            <jsp:include page="<%= userSearch%>" flush="true"/>
        </td>
        <td valign="top">
            <br><br><br>
            <internal:jahiaButton img="select_all"
                href="javascript:selectAllOptionsSelectBox(document.mainForm.selectedUsers);"
                altBundle="JahiaInternalResources" altKey="org.jahia.admin.users.GroupMembersTool.altSelectAllCandidates.label" />
            <br>
            <internal:jahiaButton img="invert_selection"
                href="javascript:invertSelectionSelectBox(document.mainForm.selectedUsers);"
                altBundle="JahiaInternalResources" altKey="org.jahia.admin.users.GroupMembersTool.altInvertCandidatesSelection.label" />
        </td>
    </tr>
</table>
<br>
<table class="text" border="0" width="95%">
    <tr>
        <td align="center" >
        <div class="buttonList" style="padding-top: 8px; padding-bottom: 8px">
        <div class="button" title="<fmt:message key='org.jahia.admin.users.ManageGroups.altApplyAndReturn.label'/>">
<a href="javascript:pasteSelectionClose()" ><fmt:message key="org.jahia.admin.ok.label"/></a></div>
         <div class="button" title="<fmt:message key='org.jahia.admin.users.GroupMembersTool.altCloseWithoutPaste.label'/>">
		<a href="javascript:window.close();" ><fmt:message key="org.jahia.admin.cancel.label"/></a>
</div>
<div class="button" title="<fmt:message key='org.jahia.admin.users.GroupMembersTool.altPasteWithoutClose.label'/>">
		<a href="javascript:pasteSelection();" ><fmt:message key="org.jahia.admin.select.label"/></a>
</div>


</div>
        </td>
    </tr>
</table>
<!-- Message displaying zone -->
<table class="text" border="0" width="80%">
    <tr>
        <td colspan="4" align="left" class="text2">
            <%= groupMessage%>
        </td>
    </tr>
</table>
<!--  -->
