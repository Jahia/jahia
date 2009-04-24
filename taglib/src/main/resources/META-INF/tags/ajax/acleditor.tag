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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag body-content="empty" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="aclId" required="false" rtexprvalue="true" type="java.lang.Integer" description="text" %>
<%@ attribute name="newAcl" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>
<%@ attribute name="sessionIdentifier" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="readOnly" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>
<%@ attribute name="aclContext" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="gwtacleditor" jahiaType="gwtacleditor" aclid="<%= aclId %>"
                         readOnly="<%= readOnly != null ? readOnly : Boolean.FALSE.toString() %>"
                         newAcl="<%= newAcl %>" sessionIdentifier="<%= sessionIdentifier %>" templateUsage="false" aclContext="<%=aclContext%>"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                            aliasResourceName="ae_principal"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                            aliasResourceName="ae_restore_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                            aliasResourceName="ae_inherited_from"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inherited.label"
                            aliasResourceName="ae_inherited"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                            aliasResourceName="ae_restore_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                            aliasResourceName="ae_break_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                            aliasResourceName="ae_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                            aliasResourceName="ae_save"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                            aliasResourceName="ae_restore"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                            aliasResourceName="um_adduser"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                            aliasResourceName="um_addgroup"/>
    
