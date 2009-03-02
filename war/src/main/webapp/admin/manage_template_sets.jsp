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