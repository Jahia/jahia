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
<%@include file="/admin/include/header.inc"%>
<div id="topTitle">
    <div id="topTitleLogo"><img src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" /></div>
    <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageTemplateSets.label" /></h1>
</div>

<div id="adminMainContent">

<h2><fmt:message key="org.jahia.admin.manageTemplates.label" />:</h2>

<form name="mainForm" action="?do=templates" method="post">
<table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
    width="100%">
    <thead>
        <tr>
            <th width="50%"><fmt:message key="org.jahia.admin.name.label"/></th>
            <th width="25%"><fmt:message key="org.jahia.admin.components.ManageComponents.context.label"/></th>
            <th width="25%"><fmt:message key="org.jahia.admin.components.ManageComponents.deleteComponents.label"/></th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td colspan="3" class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.noApplicationFound.label"/></td>
        </tr>
    </tbody>
</table>
</form>
<div id="operationMenu">
<div id="operationMenuLabel"><fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:</div>
<ul id="operationList">
    <li class="operationEntry"><a class="operationLink"
        href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=displaynewlist")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.deployNewTemplates.label"/></a>
    </li>
    <li class="operationEntry"><a class="operationLink"
        href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=add")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.manuallyAddNewTemplate.label"/></a>
    </li>
    <li class="operationEntry"><a class="operationLink"
        href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=options")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.templatesManagementOptions.label"/></a>
    </li>
    <li class="operationEntry"><a class="operationLink"
        href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a></li>
</ul>
</div>
</div>

<%@include file="/admin/include/footer.inc"%>