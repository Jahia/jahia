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
            <br/><br/><br/><br/>
            <span class="dex-PushButton">
                <span class="first-child">
                    <a class="ico-selection-all"
                       href="#select-all" onclick="selectAllOptionsSelectBox(document.mainForm.selectMember); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altSelectAllGroupMembers.label"/></a>
                </span>
            </span>
            <br/>
            <span class="dex-PushButton">
                <span class="first-child">
                    <a class="ico-selection-invert"
                       href="#select-all" onclick="invertSelectionSelectBox(document.mainForm.selectMember); return false;"><fmt:message key="org.jahia.admin.users.ManageGroups.altInvertGroupMembersSelection.label"/></a>
                </span>
            </span>
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
